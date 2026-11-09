package com.hireforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GroqClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final boolean invalidApiKey;
    private final String model;

    @Autowired
    public GroqClient(
            ObjectMapper objectMapper,
            @Value("${groq.api.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.model:openai/gpt-oss-120b}") String model,
            @Value("${groq.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${groq.read-timeout-ms:45000}") int readTimeoutMs
    ) {
        this(objectMapper, createClient(baseUrl, connectTimeoutMs, readTimeoutMs), apiKey, model);
    }

    GroqClient(ObjectMapper objectMapper, RestClient restClient, String apiKey, String model) {
        this.objectMapper = objectMapper;
        this.restClient = restClient;
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.invalidApiKey = containsInvalidHeaderCharacters(this.apiKey);
        this.model = model;
    }

    public JsonNode completeJson(
            String systemPrompt,
            String userPrompt,
            String schemaName,
            Map<String, Object> schema
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GroqException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI career tools are not configured. Set GROQ_API_KEY on the backend.");
        }
        if (invalidApiKey) {
            throw new GroqException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The backend Groq API key configuration contains invalid characters. "
                            + "Remove embedded whitespace or control characters and restart the backend.");
        }

        Map<String, Object> responseFormat = Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", schemaName,
                        "strict", true,
                        "schema", schema
                )
        );
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", responseFormat,
                "max_completion_tokens", 3000
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String content = response == null
                    ? null
                    : response.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new GroqException(HttpStatus.BAD_GATEWAY,
                        "The AI service returned an empty response. Please try again.");
            }
            try {
                return objectMapper.readTree(content);
            } catch (Exception exception) {
                throw new GroqException(HttpStatus.BAD_GATEWAY,
                        "The AI service returned an unreadable response. Please try again.");
            }
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 429) {
                throw new GroqException(HttpStatus.TOO_MANY_REQUESTS,
                        "The AI service is at its request limit. Please wait briefly and try again.");
            }
            if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
                throw new GroqException(HttpStatus.SERVICE_UNAVAILABLE,
                        "The AI service is not available. Check the backend Groq configuration.");
            }
            if (exception.getStatusCode().value() == 408 || exception.getStatusCode().value() == 504) {
                throw new GroqException(HttpStatus.GATEWAY_TIMEOUT,
                        "The AI service took too long to respond. Please try again.");
            }
            throw new GroqException(HttpStatus.BAD_GATEWAY,
                    "The AI service could not complete this request. Please try again.");
        } catch (ResourceAccessException exception) {
            if (hasTimeoutCause(exception)) {
                throw new GroqException(HttpStatus.GATEWAY_TIMEOUT,
                        "The AI service took too long to respond. Please try again.");
            }
            throw new GroqException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI service could not be reached. Please try again shortly.");
        } catch (RestClientException exception) {
            throw new GroqException(HttpStatus.BAD_GATEWAY,
                    "The AI service returned an unreadable response. Please try again.");
        }
    }

    private static RestClient createClient(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    private static boolean containsInvalidHeaderCharacters(String value) {
        return value.chars().anyMatch(character ->
                character <= 0x20 || character >= 0x7f || Character.isWhitespace(character));
    }

    private boolean hasTimeoutCause(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof java.net.http.HttpTimeoutException
                    || current instanceof java.net.SocketTimeoutException) return true;
            current = current.getCause();
        }
        return false;
    }
}
