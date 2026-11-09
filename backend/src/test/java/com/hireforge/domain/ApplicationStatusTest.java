package com.hireforge.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationStatusTest {

    @Test
    void fromValueAcceptsLabelAndEnumName() {
        assertThat(ApplicationStatus.fromValue("Saved")).isEqualTo(ApplicationStatus.SAVED);
        assertThat(ApplicationStatus.fromValue("applied")).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(ApplicationStatus.fromValue("INTERVIEW")).isEqualTo(ApplicationStatus.INTERVIEW);
    }

    @Test
    void fromValueRejectsUnknownStatus() {
        assertThatThrownBy(() -> ApplicationStatus.fromValue("Dreaming"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown application status");
    }
}
