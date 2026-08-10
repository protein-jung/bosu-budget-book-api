package com.bosu.housebook.transaction.dto;

import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.transaction.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        LocalDate transactionDate,
        Long categoryId,
        String categoryName,
        String categoryColor,
        String categoryIcon,
        Long cardId,
        String cardName,
        Long userId,
        String userName,
        String memo) {

    public static TransactionResponse from(Transaction transaction) {
        var card = transaction.getCard();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getCategory().getColor(),
                transaction.getCategory().getIcon(),
                card != null ? card.getId() : null,
                card != null ? card.getName() : null,
                transaction.getUser().getId(),
                transaction.getUser().getName(),
                transaction.getMemo());
    }
}
