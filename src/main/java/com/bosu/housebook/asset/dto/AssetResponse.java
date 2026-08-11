package com.bosu.housebook.asset.dto;

import com.bosu.housebook.asset.AccountCategory;
import com.bosu.housebook.asset.Asset;
import com.bosu.housebook.asset.AssetType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetResponse(
        Long id,
        AssetType type,
        String name,
        String custodian,
        String symbol,
        BigDecimal quantity,
        BigDecimal averagePrice,
        BigDecimal manualValue,
        BigDecimal currentPrice,
        LocalDateTime priceUpdatedAt,
        BigDecimal currentValue,
        String memo,
        String address,
        String dong,
        String ho,
        String lawdCd,
        String complexName,
        String regionDongName,
        AccountCategory accountCategory,
        Long ownerUserId,
        String ownerName) {

    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getType(),
                asset.getName(),
                asset.getCustodian(),
                asset.getSymbol(),
                asset.getQuantity(),
                asset.getAveragePrice(),
                asset.getManualValue(),
                asset.getCurrentPrice(),
                asset.getPriceUpdatedAt(),
                asset.getCurrentValue(),
                asset.getMemo(),
                asset.getAddress(),
                asset.getDong(),
                asset.getHo(),
                asset.getLawdCd(),
                asset.getComplexName(),
                asset.getRegionDongName(),
                asset.getAccountCategory(),
                asset.getOwner() != null ? asset.getOwner().getId() : null,
                asset.getOwner() != null ? asset.getOwner().getName() : null);
    }
}
