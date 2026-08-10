package com.bosu.housebook.auth.dto;

import java.time.LocalDate;

public record TokenResponse(String accessToken, Long userId, String email, String name, LocalDate birthDate) {
}
