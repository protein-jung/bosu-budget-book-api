package com.bosu.housebook.imports.dto;

import java.math.BigDecimal;
import java.util.List;

public record ImportResultResponse(
        int importedCount,
        int skippedCount,
        BigDecimal totalAmount,
        Long cardId,
        String cardName,
        List<CategoryBreakdown> categoryBreakdown) {

    public record CategoryBreakdown(Long categoryId, String categoryName, int count, BigDecimal amount) {
    }
}
