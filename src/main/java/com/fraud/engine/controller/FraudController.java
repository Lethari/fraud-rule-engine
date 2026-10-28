package com.fraud.engine.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fraud.engine.model.FraudResult;
import com.fraud.engine.model.Transaction;
import com.fraud.engine.service.FraudEngineService;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {
    private final FraudEngineService fraudEngineService;

    public FraudController(FraudEngineService fraudEngineService) {
        this.fraudEngineService = fraudEngineService;
    }

    @PostMapping("/check")
    public ResponseEntity<FraudResult> checkFraud(@Valid @RequestBody Transaction transaction) {
        FraudResult result = fraudEngineService.evaluate(transaction);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<FraudResult>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "page must be non-negative and size must be between 1 and 100");
        }
        return ResponseEntity.ok(fraudEngineService.getHistory(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Fraud Engine is UP and running with BigDecimal engine");
    }
}
