package com.fraud.engine.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.fraud.engine.config.FraudPolicyProperties;
import com.fraud.engine.model.Transaction;

public class HighAmountRuleTest {

    private final HighAmountRule rule = new HighAmountRule(policyProperties());

    @Test
    void returnsRiskForAmountsAboveConfiguredThreshold() {
        var result = rule.evaluate(new Transaction(null, "PURCHASE", new BigDecimal("10000.01"), null, null));

        assertThat(result).contains(new FraudRule.RuleMatch(
                "High amount: 10000.01 exceeds 10000.00 [Risk: +30]", 30));
    }

    @Test
    void assignsHigherRiskAtHigherAmountBands() {
        var veryHigh = rule.evaluate(new Transaction(null, "PURCHASE", new BigDecimal("20000.01"), null, null));
        var critical = rule.evaluate(new Transaction(null, "PURCHASE", new BigDecimal("50000.01"), null, null));

        assertThat(veryHigh).hasValueSatisfying(match -> assertThat(match.riskScore()).isEqualTo(50));
        assertThat(critical).hasValueSatisfying(match -> assertThat(match.riskScore()).isEqualTo(80));
    }

    @Test
    void doesNotFlagAmountAtThreshold() {
        assertThat(rule.evaluate(new Transaction(null, "PURCHASE", new BigDecimal("10000.00"), null, null))).isEmpty();
    }

    private static FraudPolicyProperties policyProperties() {
        FraudPolicyProperties properties = new FraudPolicyProperties();
        FraudPolicyProperties.Amount amount = properties.getRules().getAmount();
        amount.setThreshold(new BigDecimal("10000.00"));
        amount.setHighThreshold(new BigDecimal("20000.00"));
        amount.setCriticalThreshold(new BigDecimal("50000.00"));
        amount.setScore(30);
        amount.setHighScore(50);
        amount.setCriticalScore(80);
        return properties;
    }
}
