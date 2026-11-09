package com.hireforge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class GroqClientTest {
    private static final String BASE_URL = "https://api.groq.com/openai/v1";
    private MockRestServiceServer server;

    private GroqClient client(String apiKey) {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        return new GroqClient(new ObjectMapper(), builder.build(), apiKey, "openai/gpt-oss-120b");
    }

    @AfterEach
    void verifyRequests() {
        if (server != null) server.verify();
    }

    @Test
    void sendsStructuredRequestAndParsesMockedGroqResponse() {
        GroqClient client = client("test-key-never-use-in-production");
        server.expect(requestTo(BASE_URL + "/chat/completions"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer test-key-never-use-in-production"))
                .andExpect(jsonPath("$.model").value("openai/gpt-oss-120b"))
                .andExpect(jsonPath("$.response_format.json_schema.strict").value(true))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"{\\"ok\\":true,\\"score\\":88}"}}]}
                        """, MediaType.APPLICATION_JSON));

        var result = client.completeJson("system", "resume text", "review", Map.of(
                "type", "object", "properties", Map.of("score", Map.of("type", "integer"))));

        assertThat(result.path("ok").asBoolean()).isTrue();
        assertThat(result.path("score").asInt()).isEqualTo(88);
    }

    @Test
    void mapsGroqRateLimitWithoutLeakingProviderResponse() {
        GroqClient client = client("test-key");
        server.expect(requestTo(BASE_URL + "/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("provider response containing sensitive details"));

        assertThatThrownBy(() -> client.completeJson("system", "input", "review", Map.of()))
                .isInstanceOfSatisfying(GroqException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(exception.getMessage()).doesNotContain("sensitive details", "test-key");
                });
    }

    @Test
    void missingApiKeyFailsBeforeMakingARequest() {
        GroqClient client = client("");

        assertThatThrownBy(() -> client.completeJson("system", "input", "review", Map.of()))
                .isInstanceOfSatisfying(GroqException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(exception.getMessage()).doesNotContain("key value");
                });
    }

    @Test
    void trimsOuterWhitespaceBeforeBuildingAuthorizationHeader() {
        GroqClient client = client(" \t test-key-with-padding \r\n");
        server.expect(requestTo(BASE_URL + "/chat/completions"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer test-key-with-padding"))
                .andExpect(jsonPath("$.model").value("openai/gpt-oss-120b"))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"{\\"ok\\":true}"}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.completeJson("system", "input", "review", Map.of()).path("ok").asBoolean())
                .isTrue();
    }

    @Test
    void rejectsEmbeddedWhitespaceAndControlCharactersWithoutMakingARequestOrLeakingKey() {
        for (String malformedKey : new String[]{"test key", "test\r\nkey", "test\u0000key"}) {
            GroqClient client = client(malformedKey);

            assertThatThrownBy(() -> client.completeJson("system", "input", "review", Map.of()))
                    .isInstanceOfSatisfying(GroqException.class, exception -> {
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                        assertThat(exception.getMessage()).contains("invalid characters");
                        assertThat(exception.getMessage()).doesNotContain("test key", "test\r\nkey", "test\u0000key");
                    });
            server.verify();
            server = null;
        }
    }
}
