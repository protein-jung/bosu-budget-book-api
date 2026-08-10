package com.bosu.housebook.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UserUpdateRequest(@NotNull @Past LocalDate birthDate) {
}
