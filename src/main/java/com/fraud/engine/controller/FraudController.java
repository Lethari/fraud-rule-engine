package com.fraud.engine.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.repository.FraudResultRepository;
import com.fraud.engine.service.FraudEngineService;

@RestController
@RequestMapping("/api/fraud")
@CrossOrigin(origins = "*") //no restriction on the origin of the request

public class FraudController {
    private final FraudEngineService fraudEngineService;
    private final FraudResultRepository fraudResultRepository;

    public FraudController(FraudEngineService fraudEngineService, FraudResultRepository fraudResultRepository) {
        this.fraudEngineService = fraudEngineService;
        this.fraudResultRepository = fraudResultRepository;

    }
    // Endpoint to check if a transaction is fraudulent
    @SuppressWarnings("null")
    @PostMapping("/check")
    public ResponseEntity<FraudResult> checkFraud(@RequestBody Transaction transaction) {
        FraudResult result = fraudEngineService.evaluate(transaction);
        fraudResultRepository.save(result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<FraudResult>> getHistory() {
        return ResponseEntity.ok(fraudResultRepository.findAll());
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Fraud Engine is UP and running with BigDecimal engine");
    }
}

