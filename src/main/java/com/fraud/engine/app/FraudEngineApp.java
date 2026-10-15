package com.fraud.engine.app;

import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.rules.ForeignCountryRule;
import com.fraud.engine.rules.HighAmountRule;
import com.fraud.engine.rules.NightTimeRule;
import com.fraud.engine.service.FraudEngineService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FraudEngineApp {
    public static void main(String[] args) {
        System.out.println("=== Starting Fraud Rule Engine v1.0.0 (BigDecimal Edition) ===");

        var rules = List.of(
            new HighAmountRule(),
            new ForeignCountryRule(),
            new NightTimeRule()
        );

        var engine = new FraudEngineService(rules);
        // Sample transactions for testing
        List<Transaction> testTransactions = List.of(
            new Transaction("TXN-001", new BigDecimal("500.00"), "ZA", LocalDateTime.of(2026, 10, 15, 10, 30), "4111111111111111"),
            new Transaction("TXN-002", new BigDecimal("25000.00"), "ZA", LocalDateTime.of(2026, 10, 15, 2, 15), "4111111111111111"),
            new Transaction("TXN-003", new BigDecimal("2000.00"), "NG", LocalDateTime.of(2026, 10, 15, 14, 0), "4222222222222222"),
            new Transaction("TXN-004", new BigDecimal("55000.00"), "NG", LocalDateTime.of(2026, 10, 15, 3, 0), "4333333333333333"),
            new Transaction("TXN-005", new BigDecimal("15000.00"), "RU", LocalDateTime.of(2026, 10, 15, 23, 30), "4444444444444444")
        );

        for (Transaction txn : testTransactions) {
            FraudResult result = engine.evaluate(txn);
            System.out.println("\n--------------------------------------------------");
            System.out.println("Transaction: " + txn.getId() + " | R" + txn.getAmount() + " | " + txn.getCountry() + " | " + txn.getTimestamp().getHour() + "h");
            System.out.println("=> FRAUD? " + result.isFraud() + " | Total Risk Score: " + result.getRiskScore() + "/100");
            if (!result.getReasons().isEmpty()) {
                result.getReasons().forEach(r -> System.out.println(" - " + r));
            } else {
                System.out.println(" Clean transaction");
            }
        }
        System.out.println("\n=== Engine Finished ===");
    }
}
