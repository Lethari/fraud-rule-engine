package com.fraud.engine.rules;

import java.util.Optional;

import com.fraud.engine.model.Transaction;

public interface FraudRule {
    Optional<RuleMatch> evaluate(Transaction transaction);

    record RuleMatch(String reason, int riskScore) {
    }
}
