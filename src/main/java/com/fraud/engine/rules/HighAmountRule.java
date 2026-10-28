package com.fraud.engine.rules;

import java.math.BigDecimal;
import java.util.Optional;

import com.fraud.engine.config.FraudPolicyProperties;
import com.fraud.engine.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class HighAmountRule implements FraudRule {
    private final FraudPolicyProperties.Amount policy;

    public HighAmountRule(FraudPolicyProperties properties) {
        this.policy = properties.getRules().getAmount();
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction t) {
        BigDecimal amount = t.getAmount();
        if (amount.compareTo(policy.getCriticalThreshold()) > 0) {
            return Optional.of(new RuleMatch(
                    "Critical amount: " + amount + " [Risk: +" + policy.getCriticalScore() + "]",
                    policy.getCriticalScore()));
        }
        if (amount.compareTo(policy.getHighThreshold()) > 0) {
            return Optional.of(new RuleMatch(
                    "Very high amount: " + amount + " [Risk: +" + policy.getHighScore() + "]",
                    policy.getHighScore()));
        }
        if (amount.compareTo(policy.getThreshold()) > 0) {
            return Optional.of(new RuleMatch(
                    "High amount: " + amount + " exceeds " + policy.getThreshold()
                            + " [Risk: +" + policy.getScore() + "]",
                    policy.getScore()));
        }
        return Optional.empty();
    }
}