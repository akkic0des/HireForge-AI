package com.hireforge.dto;

import com.hireforge.domain.ApplicationStatus;
import com.hireforge.domain.JobApplication;

import java.time.LocalDate;

public class JobApplicationResponse {

    private Long id;
    private String company;
    private String role;
    private ApplicationStatus status;
    private String jobDescription;
    private LocalDate applicationDate;
    private String notes;

    public static JobApplicationResponse from(JobApplication application) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(application.getId());
        response.setCompany(application.getCompany());
        response.setRole(application.getRole());
        response.setStatus(application.getStatus());
        response.setJobDescription(application.getJobDescription());
        response.setApplicationDate(application.getApplicationDate());
        response.setNotes(application.getNotes());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
