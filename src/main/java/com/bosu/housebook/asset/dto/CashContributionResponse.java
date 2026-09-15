package com.bosu.housebook.asset.dto;

import com.bosu.housebook.asset.Asset;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 적금 자산의 납입 내역 한 건(예치금 1건 + 매달 납입 여러 건). */
public record CashContributionResponse(LocalDate date, BigDecimal amount, boolean initial) {

    public static CashContributionResponse from(Asset.CashContribution contribution) {
        return new CashContributionResponse(contribution.date(), contribution.amount(), contribution.initial());
    }
}
