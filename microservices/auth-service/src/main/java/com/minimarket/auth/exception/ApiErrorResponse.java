package com.minimarket.auth.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(

        Instant timestamp,

        int status,

        String error,

        String message,

        String path,

        List<FieldValidationError> fieldErrors
) {

    public static ApiErrorResponse of(
            HttpStatus status,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                List.of()
        );
    }

    public static ApiErrorResponse withFieldErrors(
            HttpStatus status,
            String message,
            String path,
            List<FieldValidationError> fieldErrors
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                List.copyOf(fieldErrors)
        );
    }
}
