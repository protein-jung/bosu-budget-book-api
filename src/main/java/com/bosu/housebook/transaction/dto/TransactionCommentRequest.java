package com.bosu.housebook.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransactionCommentRequest(@NotBlank @Size(max = 500) String body) {
}
