
package com.hireforge.service;

import com.hireforge.domain.JobApplication;
import com.hireforge.domain.User;
import com.hireforge.dto.JobApplicationRequest;
import com.hireforge.dto.JobApplicationResponse;
import com.hireforge.exception.ResourceNotFoundException;
import com.hireforge.repository.JobApplicationRepository;
import com.hireforge.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final UserRepository userRepository;

    public JobApplicationService(
            JobApplicationRepository repository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> findAll(String email) {
        User user = getUser(email);

        return repository.findAllByUserId(user.getId())
                .stream()
                .map(JobApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse findById(Long id, String email) {
        return JobApplicationResponse.from(getApplication(id, email));
    }

    public JobApplicationResponse create(
            JobApplicationRequest request,
            String email
    ) {
        User user = getUser(email);

        JobApplication application = new JobApplication();
        application.setUser(user);

        apply(application, request);

        return JobApplicationResponse.from(repository.save(application));
    }

    public JobApplicationResponse update(
            Long id,
            JobApplicationRequest request,
            String email
    ) {
        JobApplication application = getApplication(id, email);

        apply(application, request);

        return JobApplicationResponse.from(repository.save(application));
    }

    public void delete(Long id, String email) {
        JobApplication application = getApplication(id, email);
        repository.delete(application);
    }

    private User getUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
    }

    private JobApplication getApplication(Long id, String email) {
        User user = getUser(email);

        return repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job application not found: " + id
                        )
                );
    }

    private void apply(
            JobApplication application,
            JobApplicationRequest request
    ) {
        application.setCompany(request.getCompany().trim());
        application.setRole(request.getRole().trim());
        application.setStatus(request.getStatus());

        application.setJobDescription(
                trimToNull(request.getJobDescription())
        );

        application.setApplicationDate(
                request.getApplicationDate() == null
                        ? LocalDate.now()
                        : request.getApplicationDate()
        );

        application.setNotes(trimToNull(request.getNotes()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
