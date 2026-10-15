package com.fraud.engine.model;

import java.util.List;
import java.util.Collections;

public class FraudResult {
    private final String transactionId;
    private final boolean fraud;
    private final  List<String> reasons;
    private final int riskScore;

    public FraudResult(String transactionId, boolean fraud, List<String> reasons, int riskScore) {
        this.transactionId = transactionId; this.fraud = fraud;
        this.reasons = reasons; this.riskScore = riskScore;
    }
    public String getTransactionId() { return transactionId; }
    public boolean isFraud() { return fraud; }
    public List<String> getReasons() { return Collections.unmodifiableList(reasons); } //Caller cannot modify internal fraud reasons.
    public int getRiskScore() { return riskScore; }


    @Override
    public String toString() {
        return "FraudResult{id=" + transactionId + ", fraud=" + fraud + ", reasons=" + reasons + ", score=" + riskScore + "}";
    }
}