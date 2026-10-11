package com.anurag.appointment.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AccountResponse account) {
}
