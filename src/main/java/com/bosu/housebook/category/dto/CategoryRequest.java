package com.bosu.housebook.category.dto;

import com.bosu.housebook.common.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull TransactionType type,
        @Size(max = 20) String color,
        @Size(max = 8) String icon,
        Long parentId,
        @DecimalMin(value = "0", inclusive = true) BigDecimal targetAmount,
        boolean isGroup) {
}
