package com.bosu.housebook.asset;

import java.math.BigDecimal;

public record QuotedPrice(BigDecimal price, String currency) {
}
