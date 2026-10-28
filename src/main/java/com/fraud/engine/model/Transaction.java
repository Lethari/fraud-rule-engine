package com.fraud.engine.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class Transaction {
    @NotBlank
    @Size(max = 100)
    private String id;

    @NotBlank
    @Size(max = 50)
    private String eventCategory;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;

    @Size(max = 100)
    private String country;

    @NotNull
    private OffsetDateTime timestamp;

    public Transaction() {}

    public Transaction(
            String id, String eventCategory, BigDecimal amount, String country, OffsetDateTime timestamp) {
        this.id = id;
        this.eventCategory = eventCategory;
        this.amount = amount;
        this.country = country;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
}