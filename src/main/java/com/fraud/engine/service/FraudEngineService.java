package com.fraud.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fraud.engine.config.FraudPolicyProperties;
import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.repository.FraudResultRepository;
import com.fraud.engine.rules.FraudRule;

@Service
public class FraudEngineService {

    private final List<FraudRule> rules;
    private final FraudResultRepository fraudResultRepository;
    private final FraudPolicyProperties.Scoring scoring;
    private final String policyVersion;

    public FraudEngineService(
            List<FraudRule> rules,
            @Autowired(required = false) FraudResultRepository fraudResultRepository,
            FraudPolicyProperties policyProperties) {
        this.rules = rules;
        this.fraudResultRepository = fraudResultRepository;
        this.scoring = policyProperties.getScoring();
        this.policyVersion = policyProperties.getPolicyVersion();
    }

    public FraudResult evaluate(Transaction transaction) {
        if (fraudResultRepository != null) {
            try {
                var existing = fraudResultRepository.findByTransactionId(transaction.getId());
                if (existing.isPresent()) return existing.get();
            } catch (Exception ignored) {}
        }

        FraudResult result = evaluateNew(transaction);

        if (fraudResultRepository != null) {
            try {
                return fraudResultRepository.saveAndFlush(result);
            } catch (Exception e) {
                try {
                    return fraudResultRepository.findByTransactionId(transaction.getId()).orElse(result);
                } catch (Exception ex) {
                    return result;
                }
            }
        }
        return result;
    }

    public Page<FraudResult> getHistory(Pageable pageable) {
        if (fraudResultRepository != null) {
            return fraudResultRepository.findAll(pageable);
        }
        return Page.empty(pageable);
    }

    private FraudResult evaluateNew(Transaction transaction) {
        int totalScore = 0;
        List<String> reasons = new ArrayList<>();

        for (FraudRule rule : rules) {
            var match = rule.evaluate(transaction);
            if (match.isPresent()) {
                totalScore += match.get().riskScore();
                reasons.add(match.get().reason());
            }
        }

        int mediumThreshold = scoring.getMediumThreshold();
        int fraudThreshold = scoring.getFraudThreshold();
        int criticalThreshold = scoring.getCriticalThreshold();
        boolean isFraud = totalScore >= fraudThreshold;
        String category = totalScore >= criticalThreshold ? "CRITICAL"
                : totalScore >= fraudThreshold ? "HIGH"
                : totalScore >= mediumThreshold ? "MEDIUM" : "LOW";

        FraudResult result = new FraudResult();
        result.setTransactionId(transaction.getId());
        result.setPolicyVersion(policyVersion);
        result.setEventCategory(transaction.getEventCategory());
        result.setTransactionTimestamp(transaction.getTimestamp());
        result.setAmount(transaction.getAmount());
        result.setCountry(transaction.getCountry());
        result.setFraud(isFraud);
        result.setRiskScore(totalScore);
        result.setCategory(category);
        result.setReasons(reasons);
        return result;
    }
}