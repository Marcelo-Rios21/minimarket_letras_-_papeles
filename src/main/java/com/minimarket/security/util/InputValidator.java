package com.minimarket.security.util;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class InputValidator {

    private static final Pattern PATRON_PELIGROSO = Pattern.compile(
            "(?i)<\\s*/?\\s*script|javascript:|on\\w+\\s*=|[<>]"
    );

    private InputValidator() {
    }

    public static void validarTextoSeguro(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El campo " + campo + " no puede estar vacío"
            );
        }

        if (PATRON_PELIGROSO.matcher(valor).find()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El campo " + campo + " contiene caracteres no permitidos"
            );
        }
    }
}
