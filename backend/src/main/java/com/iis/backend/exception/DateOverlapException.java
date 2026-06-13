package com.iis.backend.exception;

/** Thrown when a trip's dates overlap with an existing trip. Mapped to HTTP 409 Conflict. */
public class DateOverlapException extends RuntimeException {
    public DateOverlapException(String message) {
        super(message);
    }
}
