package com.fraud.engine.rules;

import java.math.BigDecimal;
import java.util.Optional;
import com.fraud.engine.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class HighAmountRule implements FraudRule {
    private final BigDecimal threshold;
    private int lastScore = 0;

    public HighAmountRule(BigDecimal threshold) { this.threshold = threshold; }
    public HighAmountRule() { this(new BigDecimal("10000.00")); }

    @Override
    public Optional<String> apply(Transaction t) {
        BigDecimal amount = t.getAmount();
        if (amount.compareTo(new BigDecimal("50000")) > 0) { lastScore = 80; return Optional.of("CRITICAL amount: R" + amount + " [Risk: +80]"); }
        if (amount.compareTo(new BigDecimal("20000")) > 0) { lastScore = 50; return Optional.of("Very high amount: R" + amount + " [Risk: +50]"); }
        if (amount.compareTo(threshold) > 0) { lastScore = 30; return Optional.of("High amount: R" + amount + " > R" + threshold + " [Risk: +30]"); }
        lastScore = 0;
        return Optional.empty();
    }

    @Override public String getRuleName() { return "HighAmountRule"; }
    @Override public int getRiskScore() { return lastScore; }
}