package com.bosu.housebook.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryMemoRequest(@NotBlank @Size(max = 500) String memo) {
}
