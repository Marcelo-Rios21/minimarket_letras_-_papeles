package com.minimarket.auth.exception;

public record FieldValidationError(

        String field,

        String message
) {
}
