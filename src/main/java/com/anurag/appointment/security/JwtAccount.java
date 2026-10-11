package com.anurag.appointment.security;

import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtAccount {
    private JwtAccount() {
    }

    public static Long id(Jwt jwt) {
        String value = jwt.getClaimAsString("accountId");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Authenticated token has no account identifier");
        }
        return Long.valueOf(value);
    }
}
