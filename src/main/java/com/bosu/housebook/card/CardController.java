package com.bosu.housebook.card;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.card.dto.CardRequest;
import com.bosu.housebook.card.dto.CardResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<CardResponse> getAll(@CurrentUserId Long userId) {
        return cardService.getAll(userId);
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@CurrentUserId Long userId, @Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.create(userId, request));
    }

    @PutMapping("/{cardId}")
    public CardResponse update(@CurrentUserId Long userId, @PathVariable Long cardId,
            @Valid @RequestBody CardRequest request) {
        return cardService.update(userId, cardId, request);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long cardId) {
        cardService.delete(userId, cardId);
        return ResponseEntity.noContent().build();
    }
}
