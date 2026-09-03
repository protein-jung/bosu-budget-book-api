package com.bosu.housebook.admin.dto;

import java.time.LocalDateTime;

public record AdminMemberResponse(Long userId, String name, String email, String role, LocalDateTime joinedAt) {
}
