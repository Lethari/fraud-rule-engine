package com.fraud.engine.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fraud.engine.app.FraudEngineApp;
import com.fraud.engine.repository.FraudResultRepository;

@SpringBootTest(classes = FraudEngineApp.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:fraudtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "fraud.security.username=test-user",
        "fraud.security.password=test-password"
})
@AutoConfigureMockMvc
class FraudApiIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private FraudResultRepository repository;

        @BeforeEach
        void clearHistory() {
                repository.deleteAll();
        }

        @Test
        void healthIsPublicButFraudEndpointsRequireAuthentication() throws Exception {
                mockMvc.perform(get("/api/fraud/health"))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/api/fraud/history"))
                        .andExpect(status().isUnauthorized());
        }

        @Test
        void validatesTransactionsAndReturnsPaginatedHistory() throws Exception {
        String transaction = """
                {
                        "id": "txn-1",
                        "eventCategory": "PURCHASE",
                        "amount": 1.00,
                        "country": "NG",
                        "timestamp": "2026-10-27T23:00:00+02:00"
                }
                """;

        mockMvc.perform(post("/api/fraud/check")
                        .header(HttpHeaders.AUTHORIZATION, authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fraud").value(true))
                .andExpect(jsonPath("$.riskScore").value(115))
                .andExpect(jsonPath("$.eventCategory").value("PURCHASE"))
                .andExpect(jsonPath("$.policyVersion").value("demo-provisional-v1"))
                .andExpect(jsonPath("$.transactionTimestamp").exists());

        mockMvc.perform(post("/api/fraud/check")
                        .header(HttpHeaders.AUTHORIZATION, authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "id": "invalid",
                                "eventCategory": "PURCHASE",
                                "amount": -2,
                                "timestamp": "2026-10-27T23:00:00+02:00"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/fraud/history?page=0&size=10")
                        .header(HttpHeaders.AUTHORIZATION, authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].transactionId").value("txn-1"));

        mockMvc.perform(get("/api/fraud/history?page=0&size=101")
                        .header(HttpHeaders.AUTHORIZATION, authorization()))
                .andExpect(status().isBadRequest());
        }

        @Test
        void transactionRetryReturnsOriginalEvaluationWithoutCreatingAnotherRecord() throws Exception {
        String transaction = """
                {
                "id": "txn-idempotent",
                "eventCategory": "PURCHASE",
                "amount": 1.00,
                "country": "NG",
                "timestamp": "2026-10-27T23:00:00+02:00"
                }
                """;

        for (int attempt = 0; attempt < 2; attempt++) {
                mockMvc.perform(post("/api/fraud/check")
                        .header(HttpHeaders.AUTHORIZATION, authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.riskScore").value(115));
        }

        org.assertj.core.api.Assertions.assertThat(repository.count()).isEqualTo(1);
        }

        private String authorization() {
        String credentials = Base64.getEncoder()
                .encodeToString("test-user:test-password".getBytes(StandardCharsets.UTF_8));
        return "Basic " + credentials;
        }
}
