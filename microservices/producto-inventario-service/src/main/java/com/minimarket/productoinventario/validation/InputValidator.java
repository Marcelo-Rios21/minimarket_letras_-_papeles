package com.minimarket.productoinventario.validation;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern PATRON_PELIGROSO = Pattern.compile(
            "(?i)<\\s*/?\\s*script|javascript:|on\\w+\\s*=|[<>]"
    );

    private InputValidator() {
    }

    public static String normalizarTextoSeguro(
            String valor,
            String nombreCampo
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El campo " + nombreCampo + " no puede estar vacío"
            );
        }

        String valorNormalizado = valor.trim();

        if (PATRON_PELIGROSO.matcher(valorNormalizado).find()) {
            throw new IllegalArgumentException(
                    "El campo " + nombreCampo
                            + " contiene caracteres no permitidos"
            );
        }

        return valorNormalizado;
    }
}
