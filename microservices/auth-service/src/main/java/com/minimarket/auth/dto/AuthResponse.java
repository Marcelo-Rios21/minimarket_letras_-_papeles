package com.minimarket.auth.dto;

import java.util.List;

public record AuthResponse(

        String token,

        String tokenType,

        long expiresInSeconds,

        String username,

        List<String> roles
) {
}
