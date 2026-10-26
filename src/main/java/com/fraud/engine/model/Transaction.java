package com.fraud.engine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private BigDecimal amount;
    private String country;
    private LocalDateTime timestamp;
    private String cardNumber;
    
public Transaction() {}

// Constructor
public Transaction(String id, BigDecimal amount, String country, LocalDateTime timestamp, String cardNumber) {
        this.id = id;
        this.amount = amount;
        this.country = country;
        this.timestamp = timestamp;
        this.cardNumber = cardNumber;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

}