package org.example.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void paymentLifecycleAndIdempotencyShouldWork() throws Exception {
        String createBody = """
                {
                  "idempotencyKey": "itest-001",
                  "sourceAccount": "12345678",
                  "destinationAccount": "87654321",
                  "amount": 100.00,
                  "currency": "USD",
                  "reference": "integration-test"
                }
                """;

        String createdJson = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode createdNode = objectMapper.readTree(createdJson);
        String paymentId = createdNode.get("id").asText();

        mockMvc.perform(post("/api/payments/{id}/validate", paymentId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/payments/{id}/send", paymentId))
                .andExpect(status().isOk());
        String completedJson = mockMvc.perform(post("/api/payments/{id}/complete", paymentId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode completedNode = objectMapper.readTree(completedJson);
        assertThat(completedNode.get("status").asText()).isEqualTo("COMPLETED");

        String secondCreateJson = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode secondCreateNode = objectMapper.readTree(secondCreateJson);
        assertThat(secondCreateNode.get("id").asText()).isEqualTo(paymentId);

        String historyJson = mockMvc.perform(get("/api/payments/{id}/history", paymentId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode history = objectMapper.readTree(historyJson);
        assertThat(history).hasSize(4);
    }
}

