package com.fraud.engine.service;

import com.fraud.engine.model.FraudResult;
import com.fraud.engine.repository.FraudResultRepository;
import com.fraud.engine.rules.FraudRule;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class FraudEngineService {

    private final FraudResultRepository repository;
    private final List<FraudRule> rules;

    public FraudEngineService(FraudResultRepository repository, List<FraudRule> rules) {
        this.repository = repository;
        this.rules = rules;
    }

    public FraudResult checkFraud(Map<String, Object> transaction) {
        int totalScore = 0;
        StringBuilder reasons = new StringBuilder();

        double amount = ((Number) transaction.getOrDefault("amount", 0)).doubleValue();
        String country = (String) transaction.getOrDefault("country", "ZA");
        int hour = ((Number) transaction.getOrDefault("hour", 12)).intValue();

        for (FraudRule rule : rules) {
            if (rule.evaluate(transaction)) {
                totalScore += rule.getScore();
                reasons.append(rule.getReason()).append("; ");
            }
        }

        boolean isFraud = totalScore >= 50;
        String category = totalScore >= 80 ? "CRITICAL" : totalScore >= 50 ? "HIGH" : totalScore >= 20 ? "MEDIUM" : "LOW";

        FraudResult result = new FraudResult();
        result.setAmount(amount);
        result.setCountry(country);
        result.setRiskScore(totalScore);
        result.setFraud(isFraud);
        result.setCategory(category);
        result.setReason(reasons.toString());

        return repository.save(result);
    }

    public List<FraudResult> getHistory() {
        return repository.findAll();
    }
}
