package com.minimarket.productoinventario.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un campo que no superó la validación")
public record FieldValidationError(

        @Schema(example = "precio")
        String field,

        @Schema(example = "El precio debe ser mayor que cero")
        String message
) {
}
