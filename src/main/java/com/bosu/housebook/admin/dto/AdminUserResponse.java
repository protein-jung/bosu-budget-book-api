package com.bosu.housebook.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String email,
        String name,
        LocalDate birthDate,
        boolean blocked,
        LocalDateTime createdAt,
        Long householdId,
        String householdName,
        String householdRole,
        long transactionCount) {
}
