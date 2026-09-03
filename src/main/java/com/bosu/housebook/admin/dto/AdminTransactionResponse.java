package com.bosu.housebook.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminTransactionResponse(
        Long id,
        LocalDate transactionDate,
        String type,
        BigDecimal amount,
        String categoryName,
        String categoryIcon,
        String memo,
        String userName,
        String cardName) {
}
