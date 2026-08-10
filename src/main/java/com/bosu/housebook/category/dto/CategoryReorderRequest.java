package com.bosu.housebook.category.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CategoryReorderRequest(@NotEmpty List<Long> categoryIds) {
}
