package com.hireforge.controller;

import com.hireforge.domain.ApplicationStatus;
import com.hireforge.dto.JobApplicationResponse;
import com.hireforge.exception.ResourceNotFoundException;
import com.hireforge.security.CustomUserDetailsService;
import com.hireforge.security.JwtAuthenticationFilter;
import com.hireforge.security.JwtService;
import com.hireforge.config.SecurityConfig;
import com.hireforge.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class JobApplicationControllerTest {

    private static final String EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobApplicationService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = EMAIL)
    void listApplicationsReturnsOk() throws Exception {
        JobApplicationResponse response = sampleResponse();
        when(service.findAll(EMAIL)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].company").value("Stripe"))
                .andExpect(jsonPath("$[0].status").value("Interview"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void getMissingApplicationReturnsNotFound() throws Exception {
        when(service.findById(42L, EMAIL)).thenThrow(new ResourceNotFoundException("Job application not found: 42"));

        mockMvc.perform(get("/api/applications/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Job application not found: 42"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createValidApplicationReturnsCreated() throws Exception {
        JobApplicationResponse response = sampleResponse();
        when(service.create(any(com.hireforge.dto.JobApplicationRequest.class), eq(EMAIL))).thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Stripe",
                                  "role": "Frontend Engineer",
                                  "status": "Interview",
                                  "jobDescription": "Build dashboard UI",
                                  "applicationDate": "2026-10-12",
                                  "notes": "Phone screen booked"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createInvalidApplicationReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "",
                                  "role": "",
                                  "status": "Saved"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.company").exists())
                .andExpect(jsonPath("$.fieldErrors.role").exists());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateReturnsOk() throws Exception {
        JobApplicationResponse response = sampleResponse();
        response.setStatus(ApplicationStatus.OFFERED);
        when(service.update(eq(1L), any(com.hireforge.dto.JobApplicationRequest.class), eq(EMAIL))).thenReturn(response);

        mockMvc.perform(put("/api/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Stripe",
                                  "role": "Frontend Engineer",
                                  "status": "Offered"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Offered"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/applications/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteMissingReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Job application not found: 9")).when(service).delete(9L, EMAIL);

        mockMvc.perform(delete("/api/applications/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void applicationsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    private JobApplicationResponse sampleResponse() {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompany("Stripe");
        response.setRole("Frontend Engineer");
        response.setStatus(ApplicationStatus.INTERVIEW);
        response.setJobDescription("Build dashboard UI");
        response.setApplicationDate(LocalDate.of(2026, 10, 12));
        response.setNotes("Phone screen booked");
        return response;
    }
}
