package com.bosu.housebook.category.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CategoryMonthlyTargetRequest(@NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal amount) {
}
