package com.bosu.housebook.auth.dto;

public record TokenResponse(String accessToken, Long userId, String email, String name) {
}
