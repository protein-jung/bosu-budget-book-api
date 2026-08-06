package com.bosu.housebook.category.dto;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.common.TransactionType;

public record CategoryResponse(Long id, String name, TransactionType type, String color) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor());
    }
}
