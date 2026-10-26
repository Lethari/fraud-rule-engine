package com.fraud.engine.service;

import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.rules.FraudRule;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FraudEngineService {

    private final List<FraudRule> rules;

    public FraudEngineService(List<FraudRule> rules) {
        this.rules = rules;
    }

    public FraudResult evaluate(Transaction transaction) {
        int totalScore = 0;
        List<String> reasons = new ArrayList<>();
        
        // Evaluate each rule and accumulate the risk score and reasons
        for (FraudRule rule : rules) {
            Optional<String> result = rule.apply(transaction);
            if (result.isPresent()) {
                totalScore += rule.getRiskScore();
                reasons.add(result.get());
            }
        }

        boolean isFraud = totalScore >= 50;
        String category = totalScore >= 80 ? "CRITICAL" : totalScore >= 50 ? "HIGH" : totalScore >= 20 ? "MEDIUM" : "LOW";

        // Create and Return a FraudResult object with the evaluation details
        FraudResult fr = new FraudResult();
        fr.setTransactionId(transaction.getId());
        fr.setAmount(transaction.getAmount());
        fr.setCountry(transaction.getCountry());
        fr.setFraud(isFraud);
        fr.setRiskScore(totalScore);
        fr.setCategory(category);
        fr.setReasons(reasons);

        return fr;
    }
}