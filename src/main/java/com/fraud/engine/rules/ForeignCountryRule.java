package com.fraud.engine.rules;

import java.util.Map;
import java.util.Optional;
import java.util.Locale;

import com.fraud.engine.config.FraudPolicyProperties;
import org.springframework.stereotype.Component;

import com.fraud.engine.model.Transaction;

@Component
public class ForeignCountryRule implements FraudRule {

    private final FraudPolicyProperties.Country policy;
    private final Map<String, Integer> countryRiskScores;

    public ForeignCountryRule(FraudPolicyProperties properties) {
        this.policy = properties.getRules().getCountry();
        this.countryRiskScores = properties.normalizedCountryRiskScores();
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction) {
        if (transaction.getCountry() == null || transaction.getCountry().isBlank()) {
            return Optional.of(new RuleMatch(
                    "Country is missing [Risk: +" + policy.getMissingScore() + "]", policy.getMissingScore()));
        }
        String country = transaction.getCountry().trim().toUpperCase(Locale.ROOT);
        Integer score = countryRiskScores.get(country);
        if (score != null) {
            return Optional.of(new RuleMatch("High risk country: " + country + " [Risk: +" + score + "]", score));
        }
        return Optional.empty();
    }
}