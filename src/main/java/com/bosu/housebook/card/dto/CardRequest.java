package com.bosu.housebook.card.dto;

import com.bosu.housebook.card.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull CardType type,
        Long ownerUserId) {
}
