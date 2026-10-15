package com.fraud.engine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final String id;
    private final BigDecimal amount;
    private final String country;
    private final LocalDateTime timestamp;
    private final String cardNumber;

    // Constructor
    public Transaction(String id, BigDecimal amount, String country, LocalDateTime timestamp, String cardNumber) {
        this.id = id;
        this.amount = amount;
        this.country = country;
        this.timestamp = timestamp;
        this.cardNumber = cardNumber;
    }

    // Getters
    public String getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCountry() { return country; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getCardNumber() { return cardNumber; }
}