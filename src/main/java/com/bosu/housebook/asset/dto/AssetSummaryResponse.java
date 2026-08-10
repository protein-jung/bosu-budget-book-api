package com.bosu.housebook.asset.dto;

import com.bosu.housebook.asset.AssetType;
import java.math.BigDecimal;
import java.util.List;

public record AssetSummaryResponse(
        BigDecimal totalValue,
        List<TypeBreakdown> byType,
        List<CustodianBreakdown> byCustodian) {

    public record TypeBreakdown(AssetType type, BigDecimal amount) {
    }

    public record CustodianBreakdown(String custodian, BigDecimal amount) {
    }
}
