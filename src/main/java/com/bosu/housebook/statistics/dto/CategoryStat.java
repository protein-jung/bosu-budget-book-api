package com.bosu.housebook.statistics.dto;

import com.bosu.housebook.common.TransactionType;
import java.math.BigDecimal;

public record CategoryStat(Long categoryId, String categoryName, String color, TransactionType type,
        BigDecimal amount) {
}
