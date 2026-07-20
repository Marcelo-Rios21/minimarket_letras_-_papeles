package com.minimarket.productoinventario.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Respuesta estándar para errores de la API")
public record ApiErrorResponse(

        @Schema(example = "2026-07-19T10:30:00")
        LocalDateTime timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "Bad Request")
        String error,

        @Schema(example = "La solicitud contiene datos inválidos")
        String message,

        @Schema(example = "/api/productos")
        String path,

        List<FieldValidationError> validationErrors
) {
}
