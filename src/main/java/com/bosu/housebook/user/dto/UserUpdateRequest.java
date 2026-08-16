package com.bosu.housebook.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Past LocalDate birthDate) {
}
