package com.bosu.housebook.card.dto;

import com.bosu.housebook.card.Card;
import com.bosu.housebook.card.CardType;

public record CardResponse(Long id, String name, CardType type, Long ownerUserId, String ownerName) {

    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getName(),
                card.getType(),
                card.getOwner() != null ? card.getOwner().getId() : null,
                card.getOwner() != null ? card.getOwner().getName() : null);
    }
}
