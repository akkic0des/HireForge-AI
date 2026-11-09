package com.hireforge.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireforge.HireforgeApplication;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HireforgeApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "groq.api.key=")
class AuthAndIsolationIntegrationTest {
    private static final String TEST_JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrationLoginAndUserScopedApplicationCrudWorkWithSignedJwt() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String firstEmail = "owner-" + suffix + "@example.com";
        String secondEmail = "other-" + suffix + "@example.com";
        String firstToken = register(firstEmail, "correct-horse-battery");
        String secondToken = register(secondEmail, "another-password-123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"correct-horse-battery"}
                                """.formatted(firstEmail)))
                .andExpect(status().isOk())
                .andReturn();
        String loginToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("token").asText();
        assertThat(loginToken).isNotBlank();

        MvcResult createResult = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + firstToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"Acme","role":"Backend Engineer","status":"Applied","applicationDate":"2026-11-08"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long applicationId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(get("/api/applications")
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).size()).isZero());

        mockMvc.perform(get("/api/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"Hijack","role":"Role","status":"Offered","applicationDate":"2026-11-08"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/applications/{id}", applicationId)
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk());
    }

    @Test
    void expiredJwtIsRejectedAndInvalidCredentialsDoNotAuthenticate() throws Exception {
        String email = "expiry-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        register(email, "valid-password-123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"incorrect-password"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());

        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_JWT_SECRET)))
                .compact();
        mockMvc.perform(get("/api/applications")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingGroqKeyReturnsHelpfulServiceUnavailableForAuthenticatedCareerRequest() throws Exception {
        String token = register("ai-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com", "career-password-123");
        mockMvc.perform(post("/api/career/job-match")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resumeText":"Backend Java experience","jobDescription":"Java backend role"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("Set GROQ_API_KEY on the backend"));
    }

    private String register(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Portfolio User","email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("token").asText();
    }
}
