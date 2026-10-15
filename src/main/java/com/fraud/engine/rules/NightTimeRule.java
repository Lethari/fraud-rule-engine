package com.fraud.engine.rules;

import java.util.Optional;
import com.fraud.engine.model.Transaction;

public class NightTimeRule implements FraudRule {
    private int lastScore = 0;

    @Override
    public Optional<String> apply(Transaction transaction) {
        if (transaction.getTimestamp() == null) return Optional.empty();
        int hour = transaction.getTimestamp().getHour();
        
        // 00:00 - 04:59 = Most risky (people sleeping)
        if (hour >= 0 && hour <= 4) {
            lastScore = 40;
            return Optional.of("Suspicious night transaction at " + hour + ":00 [Risk: +40]");
        }
        // 22:00 - 23:59 = Risky (people winding down, less alert)
        if (hour >= 22) {
            lastScore = 20;
            return Optional.of("Late night transaction at " + hour + ":00 [Risk: +20]");
        }
        // 05:00 - 21:59 = Normal hours (no risk-low risk people are mostly awake and alert)
        lastScore = 0;
        return Optional.empty();
    }

    @Override public String getRuleName() { return "NightTimeRule"; }
    @Override public int getRiskScore() { return lastScore; }
}