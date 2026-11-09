package com.hireforge.repository;

import com.hireforge.domain.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findAllByUserId(Long userId);

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);

    List<JobApplication> findAllByUserIsNull();
}
