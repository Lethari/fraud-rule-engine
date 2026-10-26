package com.fraud.engine.rules;

import java.util.Optional;
import com.fraud.engine.model.Transaction;

public interface FraudRule {
    Optional<String> apply(Transaction transaction);
    String getRuleName();
    
    // Default implementation for getRiskScore, can be overridden by implementing classes
    default int getRiskScore() {
        return 0;
    }
}
