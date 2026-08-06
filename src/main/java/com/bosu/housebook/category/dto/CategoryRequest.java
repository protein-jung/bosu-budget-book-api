package com.bosu.housebook.category.dto;

import com.bosu.housebook.common.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull TransactionType type,
        @Size(max = 20) String color) {
}
