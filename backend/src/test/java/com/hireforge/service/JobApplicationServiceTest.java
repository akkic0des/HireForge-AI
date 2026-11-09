package com.hireforge.service;

import com.hireforge.domain.ApplicationStatus;
import com.hireforge.domain.JobApplication;
import com.hireforge.domain.User;
import com.hireforge.dto.JobApplicationRequest;
import com.hireforge.dto.JobApplicationResponse;
import com.hireforge.exception.ResourceNotFoundException;
import com.hireforge.repository.JobApplicationRepository;
import com.hireforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService service;

    private JobApplication stored;

    private User user;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        stored = new JobApplication();
        stored.setId(1L);
        stored.setCompany("Stripe");
        stored.setRole("Frontend Engineer");
        stored.setStatus(ApplicationStatus.INTERVIEW);
        stored.setApplicationDate(LocalDate.of(2026, 10, 12));
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(7L);
        stored.setUser(user);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void findAllReturnsMappedResponses() {
        when(repository.findAllByUserId(7L)).thenReturn(List.of(stored));

        List<JobApplicationResponse> result = service.findAll(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCompany()).isEqualTo("Stripe");
        assertThat(result.getFirst().getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createPersistsTrimmedFieldsAndDefaultsDate() {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setCompany("  Notion  ");
        request.setRole(" Product Designer ");
        request.setStatus(ApplicationStatus.APPLIED);
        request.setNotes("  ");

        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication application = invocation.getArgument(0);
            application.setId(8L);
            return application;
        });

        JobApplicationResponse response = service.create(request, EMAIL);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCompany()).isEqualTo("Notion");
        assertThat(captor.getValue().getRole()).isEqualTo("Product Designer");
        assertThat(captor.getValue().getApplicationDate()).isEqualTo(LocalDate.now());
        assertThat(captor.getValue().getNotes()).isNull();
        assertThat(response.getId()).isEqualTo(8L);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.findByIdAndUserId(5L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(5L, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingApplication() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(stored));

        service.delete(1L, EMAIL);

        verify(repository).delete(stored);
    }
}
