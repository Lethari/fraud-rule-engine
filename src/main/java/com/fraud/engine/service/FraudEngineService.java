package com.fraud.engine.service;

import java.util.ArrayList;
import java.util.List;

import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.rules.FraudRule;

public class FraudEngineService {

    private final List<FraudRule> rules;

    public FraudEngineService(List<FraudRule> rules) {
        this.rules = rules;
    }

    public FraudResult evaluate(Transaction transaction) {
        List<String> triggeredReasons = new ArrayList<>();
        int totalRiskScore = 0;

        for (FraudRule rule : rules) {
            var result = rule.apply(transaction);
            if (result.isPresent()) {
                triggeredReasons.add("[" + rule.getRuleName() + "] " + result.get());
                totalRiskScore += rule.getRiskScore();
            }
        }

        // Decision logic based on total score
        boolean isFraud = totalRiskScore >= 50; // threshold for blocking

        // Cap score at 100 for display
        int displayScore = Math.min(totalRiskScore, 100);

        return new FraudResult(
            transaction.getId(),
            isFraud,
            triggeredReasons,
            displayScore
        );
    }
}
