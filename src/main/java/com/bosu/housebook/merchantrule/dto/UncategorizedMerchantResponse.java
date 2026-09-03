package com.bosu.housebook.merchantrule.dto;

import com.bosu.housebook.common.TransactionType;
import com.bosu.housebook.transaction.TransactionRepository.UncategorizedMerchantProjection;
import java.math.BigDecimal;

public record UncategorizedMerchantResponse(String merchantName, TransactionType type, int count,
        BigDecimal totalAmount) {

    public static UncategorizedMerchantResponse from(UncategorizedMerchantProjection projection) {
        return new UncategorizedMerchantResponse(projection.getMemo(), projection.getType(),
                projection.getCount().intValue(), projection.getTotal());
    }
}
