package com.bosu.housebook.category.dto;

import com.bosu.housebook.category.CategoryMemo;

public record CategoryMemoResponse(Long categoryId, int year, int month, String memo) {

    public static CategoryMemoResponse from(CategoryMemo memo) {
        return new CategoryMemoResponse(memo.getCategory().getId(), memo.getYear(), memo.getMonth(), memo.getMemo());
    }
}
