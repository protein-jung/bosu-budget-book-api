package com.bosu.housebook.household.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HouseholdCreateRequest(@NotBlank @Size(max = 100) String name) {
}
