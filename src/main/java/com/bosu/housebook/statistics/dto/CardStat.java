package com.bosu.housebook.statistics.dto;

import java.math.BigDecimal;

public record CardStat(Long cardId, String cardName, BigDecimal amount) {
}
