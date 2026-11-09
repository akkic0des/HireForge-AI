package com.hireforge.service;

import org.springframework.http.HttpStatus;

public class GroqException extends RuntimeException {
    private final HttpStatus status;

    public GroqException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
