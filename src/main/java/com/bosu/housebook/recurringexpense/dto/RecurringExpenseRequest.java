package com.bosu.housebook.recurringexpense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RecurringExpenseRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 50) String name,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull @Min(1) @Max(31) Integer dayOfMonth,
        boolean active,
        @Size(max = 200) String memo) {
}
