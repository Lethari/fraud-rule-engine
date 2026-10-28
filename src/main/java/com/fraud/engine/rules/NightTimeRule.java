package com.fraud.engine.rules;

import java.util.Optional;

import com.fraud.engine.config.FraudPolicyProperties;
import com.fraud.engine.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class NightTimeRule implements FraudRule {

    private final FraudPolicyProperties.NightTime policy;

    public NightTimeRule(FraudPolicyProperties properties) {
        this.policy = properties.getRules().getNightTime();
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction) {
        if (transaction.getTimestamp() == null) return Optional.empty();
        int hour = transaction.getTimestamp().getHour();

        if (hour >= policy.getEarlyStartHour() && hour <= policy.getEarlyEndHour()) {
            return Optional.of(new RuleMatch(
                    "Early-hours transaction at " + hour + ":00 [Risk: +" + policy.getEarlyScore() + "]",
                    policy.getEarlyScore()));
        }
        if (hour >= policy.getLateStartHour() && hour <= policy.getLateEndHour()) {
            return Optional.of(new RuleMatch(
                    "Late-hours transaction at " + hour + ":00 [Risk: +" + policy.getLateScore() + "]",
                    policy.getLateScore()));
        }
        return Optional.empty();
    }
}