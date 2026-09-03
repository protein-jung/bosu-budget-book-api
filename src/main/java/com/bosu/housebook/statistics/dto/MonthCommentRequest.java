package com.bosu.housebook.statistics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MonthCommentRequest(
        @NotNull Integer year,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotBlank @Size(max = 500) String body) {
}
