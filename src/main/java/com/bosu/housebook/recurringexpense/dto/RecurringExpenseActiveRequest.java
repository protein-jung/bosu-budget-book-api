package com.bosu.housebook.recurringexpense.dto;

import jakarta.validation.constraints.NotNull;

public record RecurringExpenseActiveRequest(@NotNull Boolean active) {
}
