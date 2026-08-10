package com.bosu.housebook.imports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedTransaction(LocalDate transactionDate, String merchantName, BigDecimal amount) {
}
