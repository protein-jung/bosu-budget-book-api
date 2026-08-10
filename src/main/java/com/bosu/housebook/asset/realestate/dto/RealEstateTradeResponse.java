package com.bosu.housebook.asset.realestate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RealEstateTradeResponse(
        String aptName,
        String dong,
        String buildingDong,
        String jibun,
        BigDecimal exclusiveArea,
        Integer floor,
        LocalDate dealDate,
        long dealAmount) {
}
