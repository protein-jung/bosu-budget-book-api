package com.bosu.housebook.transaction.dto;

import com.bosu.housebook.common.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull TransactionType type,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull LocalDate transactionDate,
        @NotNull Long categoryId,
        Long cardId,
        @Size(max = 500) String memo) {
}
