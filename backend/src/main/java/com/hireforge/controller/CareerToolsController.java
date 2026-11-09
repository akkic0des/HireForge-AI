package com.hireforge.controller;

import com.hireforge.dto.CareerAiRequest;
import com.hireforge.dto.CareerAiResponse;
import com.hireforge.service.CareerAiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/career")
@Validated
public class CareerToolsController {
    private static final long MAX_PDF_BYTES = 5L * 1024 * 1024;
    private static final int MAX_RESUME_TEXT_CHARS = 18_000;

    private final CareerAiService careerAiService;

    public CareerToolsController(CareerAiService careerAiService) {
        this.careerAiService = careerAiService;
    }

    @PostMapping(value = "/resume-analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CareerAiResponse.ResumeAnalysis analyzeResume(
            @RequestPart("file") MultipartFile file,
            @RequestParam("role")
            @NotBlank(message = "Target role is required.")
            @Size(max = 150, message = "Target role must be at most 150 characters.") String role
    ) {
        if (file.isEmpty() || file.getSize() > MAX_PDF_BYTES) {
            throw new IllegalArgumentException("Choose a PDF under 5 MB.");
        }
        String filename = Objects.toString(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT);
        if (!filename.endsWith(".pdf")) {
            throw new IllegalArgumentException("Upload a file with a .pdf extension.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
                && !"application/pdf".equalsIgnoreCase(contentType)
                && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Upload a PDF resume.");
        }

        try {
            byte[] pdfBytes = file.getBytes();
            if (!hasPdfSignature(pdfBytes)) {
                throw new IllegalArgumentException("The uploaded file is not a valid PDF.");
            }
            String resumeText;
            try (var document = Loader.loadPDF(pdfBytes)) {
                resumeText = new PDFTextStripper().getText(document);
            }
            if (resumeText.isBlank()) {
                throw new IllegalArgumentException("No readable text was found. Try a text-based PDF rather than a scanned image.");
            }
            if (resumeText.length() > MAX_RESUME_TEXT_CHARS) {
                throw new IllegalArgumentException("The extracted resume text is too long. Use a shorter resume under 18000 characters.");
            }
            return careerAiService.analyzeResume(resumeText, role.trim());
        } catch (IOException exception) {
            throw new IllegalArgumentException("The PDF could not be read. Upload a valid, unencrypted PDF.");
        }
    }

    @PostMapping("/job-match")
    public CareerAiResponse.JobMatch matchJob(@Valid @RequestBody CareerAiRequest.JobMatch request) {
        return careerAiService.matchJob(request);
    }

    @PostMapping("/interview-prep")
    public CareerAiResponse.InterviewSet interviewPrep(@Valid @RequestBody CareerAiRequest.InterviewPrep request) {
        return careerAiService.prepareInterview(request);
    }

    @PostMapping("/interview-evaluate")
    public CareerAiResponse.AnswerEvaluation evaluateAnswer(@Valid @RequestBody CareerAiRequest.InterviewAnswer request) {
        return careerAiService.evaluateAnswer(request);
    }

    private boolean hasPdfSignature(byte[] bytes) {
        return bytes.length >= 5
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-';
    }
}
