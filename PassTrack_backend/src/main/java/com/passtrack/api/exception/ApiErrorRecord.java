package com.passtrack.api.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorRecord(
    Instant timestamp,
    int status,
    String error,
    String message,
    Map<String, String> details
) {
    public ApiErrorRecord(int status, String error, String message) {
        this(Instant.now(), status, error, message, Map.of());
    }
}
