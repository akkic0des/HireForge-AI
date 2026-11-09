package com.hireforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CareerAiRequest {
    private CareerAiRequest() {}

    public record JobMatch(
            @NotBlank(message = "Resume text is required.")
            @Size(max = 18000, message = "Resume text must be at most 18000 characters.")
            String resumeText,
            @NotBlank(message = "Job description is required.")
            @Size(max = 14000, message = "Job description must be at most 14000 characters.")
            String jobDescription
    ) {}

    public record InterviewPrep(
            @NotBlank(message = "Target role is required.")
            @Size(max = 150, message = "Target role must be at most 150 characters.")
            String role,
            @Size(max = 150, message = "Company must be at most 150 characters.")
            String company,
            @Size(max = 14000, message = "Job description must be at most 14000 characters.")
            String jobDescription
    ) {}

    public record InterviewAnswer(
            @NotBlank(message = "Target role is required.")
            @Size(max = 150, message = "Target role must be at most 150 characters.")
            String role,
            @NotBlank(message = "Interview question is required.")
            @Size(max = 2000, message = "Question must be at most 2000 characters.")
            String question,
            @NotBlank(message = "Your answer is required.")
            @Size(max = 6000, message = "Answer must be at most 6000 characters.")
            String answer,
            @Size(max = 150, message = "Company must be at most 150 characters.")
            String company
    ) {}
}
