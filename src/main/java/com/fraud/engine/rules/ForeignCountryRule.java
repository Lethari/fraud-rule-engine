package com.fraud.engine.rules;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fraud.engine.model.Transaction;

@Component
public class ForeignCountryRule implements FraudRule {

    //Defining a static map of high-risk countries and their associated risk scores
    private static final Map<String, Integer> COUNTRY_RISK_SCORE = Map.ofEntries(
        Map.entry("NG", 95), Map.entry("NIGERIA", 95),
        Map.entry("RU", 90), Map.entry("RUSSIA", 90),
        Map.entry("GH", 85), Map.entry("GHANA", 85),
        Map.entry("CM", 80), Map.entry("CAMEROON", 80),
        Map.entry("PK", 80), Map.entry("PAKISTAN", 80),
        Map.entry("UA", 75), Map.entry("UKRAINE", 75),
        Map.entry("ID", 70), Map.entry("INDONESIA", 70),
        Map.entry("RO", 70), Map.entry("ROMANIA", 70),
        Map.entry("VN", 65), Map.entry("VIETNAM", 65),
        Map.entry("PH", 60), Map.entry("PHILIPPINES", 60)
    );

    private int lastRiskScore = 0;

    @Override
    public Optional<String> apply(Transaction transaction) {
        if (transaction.getCountry() == null || transaction.getCountry().isBlank()) {
            lastRiskScore = 30;
            return Optional.of("Country is missing [Risk: +30]");
        }
        String country = transaction.getCountry().trim().toUpperCase();
        Integer score = COUNTRY_RISK_SCORE.get(country);
        if (score != null) {
            lastRiskScore = score;
            return Optional.of("High risk country: " + country + " [Risk: +" + score + "]");
        }
        lastRiskScore = 0;
        return Optional.empty();
    }

    @Override
    public String getRuleName() {
        return "ForeignCountryRule";
    }

    @Override
    public int getRiskScore() {
        return lastRiskScore;
    }
}