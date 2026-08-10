package com.bosu.housebook.user.dto;

import com.bosu.housebook.user.User;
import java.time.LocalDate;

public record UserResponse(Long id, String email, String name, LocalDate birthDate) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getBirthDate());
    }
}
