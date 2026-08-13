package com.bosu.housebook.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AssetSnapshotResponse(
        LocalDate date,
        BigDecimal totalValue,
        List<AssetSummaryResponse.TypeBreakdown> byType) {
}
