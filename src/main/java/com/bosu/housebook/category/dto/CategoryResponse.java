package com.bosu.housebook.category.dto;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.common.TransactionType;
import java.math.BigDecimal;

public record CategoryResponse(
        Long id,
        String name,
        TransactionType type,
        String color,
        String icon,
        Long parentId,
        Integer sortOrder,
        BigDecimal targetAmount,
        boolean isGroup) {

    public static CategoryResponse from(Category category) {
        Long parentId = category.getParent() == null ? null : category.getParent().getId();
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColor(),
                category.getIcon(),
                parentId,
                category.getSortOrder(),
                category.getTargetAmount(),
                category.isGroup());
    }
}
