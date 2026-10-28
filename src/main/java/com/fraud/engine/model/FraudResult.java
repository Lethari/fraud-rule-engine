package com.fraud.engine.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "fraud_result",
    uniqueConstraints = @UniqueConstraint(name = "uk_fraud_result_transaction_id", columnNames = "transaction_id")
)
public class FraudResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @Column(name = "policy_version", nullable = false, length = 100)
    private String policyVersion;

    @Column(name = "event_category", nullable = false, length = 50)
    private String eventCategory;

    @Column(name = "transaction_timestamp")
    private OffsetDateTime transactionTimestamp;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String country;

    private boolean fraud;
    private int riskScore;

    @Column(nullable = false, length = 20)
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fraud_result_reasons", joinColumns = @JoinColumn(name = "fraud_result_id"))
    @Column(name = "reason", length = 500)
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

    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }

    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }

    public OffsetDateTime getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(OffsetDateTime transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

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