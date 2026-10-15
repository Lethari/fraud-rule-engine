package com.fraud.engine.rules;

import java.util.Optional;
import com.fraud.engine.model.Transaction;

public interface FraudRule {
    Optional<String> apply(Transaction transaction);
    String getRuleName();
    
    // New: each rule tells how much risk it adds
    default int getRiskScore() {
        return 0;
    }
}
