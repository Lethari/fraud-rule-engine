package com.fraud.engine.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FraudResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private BigDecimal amount;
    private String country;
    private boolean fraud;
    private int riskScore;
    private String category; // HIGH, MEDIUM, LOW

    @ElementCollection
    private List<String> reasons = new ArrayList<>();

    public FraudResult() {}

    // Constructor for creating a FraudResult with all fields
    public FraudResult(String transactionId, boolean fraud, List<String> reasons, int riskScore) {
        this.transactionId = transactionId;
        this.fraud = fraud;
        this.reasons = reasons;
        this.riskScore = riskScore;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isFraud() { return fraud; }
    public void setFraud(boolean fraud) { this.fraud = fraud; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}