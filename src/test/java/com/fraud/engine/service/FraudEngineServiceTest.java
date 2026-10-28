package com.fraud.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fraud.engine.config.FraudPolicyProperties;
import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.repository.FraudResultRepository;
import com.fraud.engine.rules.ForeignCountryRule;
import com.fraud.engine.rules.HighAmountRule;
import com.fraud.engine.rules.NightTimeRule;

public class FraudEngineServiceTest {

    private FraudResultRepository repository;
    private FraudEngineService service;

    @BeforeEach
    void setUp() {
        repository = mock(FraudResultRepository.class);
        when(repository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(FraudResult.class))).thenAnswer(invocation ->
                invocation.getArgument(0, FraudResult.class));
        FraudPolicyProperties properties = policyProperties();
        service = new FraudEngineService(
                List.of(new HighAmountRule(properties), new ForeignCountryRule(properties),
                        new NightTimeRule(properties)), repository, properties);
    }

    @Test
    void calculatesEachTransactionIndependently() {
        Transaction highRisk = transaction("high", "30000", "NG", "2026-10-27T23:00:00+02:00");
        Transaction lowRisk = transaction("low", "1", "ZA", "2026-10-27T12:00:00+02:00");

        FraudResult first = service.evaluate(highRisk);
        FraudResult second = service.evaluate(lowRisk);

        assertThat(first.getRiskScore()).isEqualTo(165);
        assertThat(first.getCategory()).isEqualTo("CRITICAL");
        assertThat(first.isFraud()).isTrue();
        assertThat(second.getRiskScore()).isZero();
        assertThat(second.getCategory()).isEqualTo("LOW");
        assertThat(second.isFraud()).isFalse();
        assertThat(first.getEventCategory()).isEqualTo("PURCHASE");
        assertThat(first.getPolicyVersion()).isEqualTo("test-policy-v1");
    }

    @Test
    void appliesConfiguredFraudAndRiskBandThresholds() {
        FraudPolicyProperties properties = policyProperties();
        properties.getScoring().setMediumThreshold(40);
        properties.getScoring().setFraudThreshold(90);
        properties.getScoring().setCriticalThreshold(120);
        FraudEngineService configuredService = new FraudEngineService(
                List.of(new HighAmountRule(properties), new ForeignCountryRule(properties),
                        new NightTimeRule(properties)), repository, properties);

        FraudResult result = configuredService.evaluate(
                transaction("configured", "30000", "ZA", "2026-10-27T12:00:00+02:00"));

        assertThat(result.getRiskScore()).isEqualTo(50);
        assertThat(result.getCategory()).isEqualTo("MEDIUM");
        assertThat(result.isFraud()).isFalse();
    }

    @Test
    void returnsPreviouslyStoredResultForDuplicateTransaction() {
        FraudResult existing = new FraudResult();
        existing.setTransactionId("duplicate");
        when(repository.findByTransactionId("duplicate")).thenReturn(Optional.of(existing));

        FraudResult result = service.evaluate(transaction("duplicate", "999999", "NG", "2026-10-27T23:00:00+02:00"));

        assertThat(result).isSameAs(existing);
        verify(repository, never()).saveAndFlush(any(FraudResult.class));
    }

    @Test
    void concurrentDuplicateReturnsTheResultPersistedByTheOtherRequest() {
        FraudResult existing = new FraudResult();
        existing.setTransactionId("race");
        when(repository.findByTransactionId("race"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(any(FraudResult.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate transaction id"));

        FraudResult result = service.evaluate(transaction("race", "1", "ZA", "2026-10-27T12:00:00+02:00"));

        assertThat(result).isSameAs(existing);
    }

    private Transaction transaction(String id, String amount, String country, String timestamp) {
        return new Transaction(id, "PURCHASE", new BigDecimal(amount), country, OffsetDateTime.parse(timestamp));
    }

    private FraudPolicyProperties policyProperties() {
        FraudPolicyProperties properties = new FraudPolicyProperties();
        properties.setPolicyVersion("test-policy-v1");
        FraudPolicyProperties.Amount amount = properties.getRules().getAmount();
        amount.setThreshold(new BigDecimal("10000.00"));
        amount.setHighThreshold(new BigDecimal("20000.00"));
        amount.setCriticalThreshold(new BigDecimal("50000.00"));
        amount.setScore(30);
        amount.setHighScore(50);
        amount.setCriticalScore(80);
        properties.getRules().getCountry().setMissingScore(30);
        properties.getRules().getCountry().setRiskScores(java.util.Map.of("NG", 95, "NIGERIA", 95));
        FraudPolicyProperties.NightTime night = properties.getRules().getNightTime();
        night.setEarlyStartHour(0);
        night.setEarlyEndHour(4);
        night.setEarlyScore(40);
        night.setLateStartHour(22);
        night.setLateEndHour(23);
        night.setLateScore(20);
        properties.getScoring().setMediumThreshold(20);
        properties.getScoring().setFraudThreshold(50);
        properties.getScoring().setCriticalThreshold(80);
        return properties;
    }
}
