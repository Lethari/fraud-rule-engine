package com.fraud.engine.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "fraud")
public class FraudPolicyProperties {

    @Valid
    private final Rules rules = new Rules();

    @Valid
    private final Scoring scoring = new Scoring();

    @NotBlank
    @Size(max = 100)
    private String policyVersion;

    public Rules getRules() {
        return rules;
    }

    public Scoring getScoring() {
        return scoring;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    @PostConstruct
    void validatePolicy() {
        Amount amount = rules.amount;
        if (amount.threshold == null || amount.highThreshold == null || amount.criticalThreshold == null
                || amount.threshold.compareTo(amount.highThreshold) >= 0
                || amount.highThreshold.compareTo(amount.criticalThreshold) >= 0) {
            throw new IllegalStateException("Fraud amount thresholds must be strictly increasing");
        }

        NightTime nightTime = rules.nightTime;
        if (nightTime.earlyStartHour > nightTime.earlyEndHour
                || nightTime.lateStartHour > nightTime.lateEndHour) {
            throw new IllegalStateException("Fraud time-rule start hours must not exceed their end hours");
        }
        if (nightTime.earlyEndHour >= nightTime.lateStartHour
                && nightTime.lateEndHour >= nightTime.earlyStartHour) {
            throw new IllegalStateException("Fraud time-rule hour ranges must not overlap");
        }

        if (scoring.mediumThreshold > scoring.fraudThreshold
                || scoring.fraudThreshold > scoring.criticalThreshold) {
            throw new IllegalStateException("Fraud scoring thresholds must be nondecreasing");
        }

        if (rules.country.riskScores == null) {
            throw new IllegalStateException("Country risk scores must not be null");
        }
        rules.country.riskScores.forEach((country, score) -> {
            if (country.isBlank() || score == null || score < 0 || score > 100) {
                throw new IllegalStateException("Country risk scores must have a country key and a score from 0 to 100");
            }
        });
        if (normalizedCountryRiskScores().size() != rules.country.riskScores.size()) {
            throw new IllegalStateException("Country risk score keys must be unique regardless of letter case");
        }
    }

    public static class Rules {
        @Valid
        private final Amount amount = new Amount();

        @Valid
        private final Country country = new Country();

        @Valid
        private final NightTime nightTime = new NightTime();

        public Amount getAmount() {
            return amount;
        }

        public Country getCountry() {
            return country;
        }

        public NightTime getNightTime() {
            return nightTime;
        }
    }

    public static class Amount {
        @NotNull
        @Positive
        private BigDecimal threshold = BigDecimal.ZERO;

        @NotNull
        @Positive
        private BigDecimal highThreshold = BigDecimal.ZERO;

        @NotNull
        @Positive
        private BigDecimal criticalThreshold = BigDecimal.ZERO;

        @Min(0)
        @Max(100)
        private int score;

        @Min(0)
        @Max(100)
        private int highScore;

        @Min(0)
        @Max(100)
        private int criticalScore;

        public BigDecimal getThreshold() { return threshold; }
        public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
        public BigDecimal getHighThreshold() { return highThreshold; }
        public void setHighThreshold(BigDecimal highThreshold) { this.highThreshold = highThreshold; }
        public BigDecimal getCriticalThreshold() { return criticalThreshold; }
        public void setCriticalThreshold(BigDecimal criticalThreshold) { this.criticalThreshold = criticalThreshold; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getHighScore() { return highScore; }
        public void setHighScore(int highScore) { this.highScore = highScore; }
        public int getCriticalScore() { return criticalScore; }
        public void setCriticalScore(int criticalScore) { this.criticalScore = criticalScore; }
    }

    public static class Country {
        @Min(0)
        @Max(100)
        private int missingScore;

        private Map<String, Integer> riskScores = new LinkedHashMap<>();

        public int getMissingScore() { return missingScore; }
        public void setMissingScore(int missingScore) { this.missingScore = missingScore; }
        public Map<String, Integer> getRiskScores() { return riskScores; }
        public void setRiskScores(Map<String, Integer> riskScores) { this.riskScores = riskScores; }
    }

    public static class NightTime {
        @Min(0)
        @Max(23)
        private int earlyStartHour;

        @Min(0)
        @Max(23)
        private int earlyEndHour;

        @Min(0)
        @Max(23)
        private int lateStartHour;

        @Min(0)
        @Max(23)
        private int lateEndHour;

        @Min(0)
        @Max(100)
        private int earlyScore;

        @Min(0)
        @Max(100)
        private int lateScore;

        public int getEarlyStartHour() { return earlyStartHour; }
        public void setEarlyStartHour(int earlyStartHour) { this.earlyStartHour = earlyStartHour; }
        public int getEarlyEndHour() { return earlyEndHour; }
        public void setEarlyEndHour(int earlyEndHour) { this.earlyEndHour = earlyEndHour; }
        public int getLateStartHour() { return lateStartHour; }
        public void setLateStartHour(int lateStartHour) { this.lateStartHour = lateStartHour; }
        public int getLateEndHour() { return lateEndHour; }
        public void setLateEndHour(int lateEndHour) { this.lateEndHour = lateEndHour; }
        public int getEarlyScore() { return earlyScore; }
        public void setEarlyScore(int earlyScore) { this.earlyScore = earlyScore; }
        public int getLateScore() { return lateScore; }
        public void setLateScore(int lateScore) { this.lateScore = lateScore; }
    }

    public static class Scoring {
        @Min(0)
        private int mediumThreshold;

        @Min(0)
        private int fraudThreshold;

        @Min(0)
        private int criticalThreshold;

        public int getMediumThreshold() { return mediumThreshold; }
        public void setMediumThreshold(int mediumThreshold) { this.mediumThreshold = mediumThreshold; }
        public int getFraudThreshold() { return fraudThreshold; }
        public void setFraudThreshold(int fraudThreshold) { this.fraudThreshold = fraudThreshold; }
        public int getCriticalThreshold() { return criticalThreshold; }
        public void setCriticalThreshold(int criticalThreshold) { this.criticalThreshold = criticalThreshold; }
    }

    public Map<String, Integer> normalizedCountryRiskScores() {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        rules.country.riskScores.forEach((country, score) ->
                normalized.put(country.trim().toUpperCase(Locale.ROOT), score));
        return Map.copyOf(normalized);
    }
}
