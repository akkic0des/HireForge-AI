
package com.hireforge.controller;

import com.hireforge.dto.JobApplicationRequest;
import com.hireforge.dto.JobApplicationResponse;
import com.hireforge.service.JobApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobApplicationResponse> findAll(
            @AuthenticationPrincipal UserDetails user
    ) {
        return service.findAll(user.getUsername());
    }

    @GetMapping("/{id}")
    public JobApplicationResponse findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        return service.findById(id, user.getUsername());
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        JobApplicationResponse created =
                service.create(request, user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return service.update(id, request, user.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        service.delete(id, user.getUsername());

        return ResponseEntity.noContent().build();
    }
}
