package com.bosu.housebook.asset.dto;

import com.bosu.housebook.asset.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * type이 STOCK/CRYPTO면 symbol+quantity가, 그 외면 manualValue가 있어야 한다 —
 * 조건부라 여기서는 형식(@Size 등)만 검증하고 필수 여부는 AssetService에서 확인한다.
 */
public record AssetRequest(
        @NotNull AssetType type,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String custodian,
        @Size(max = 20) String symbol,
        BigDecimal quantity,
        BigDecimal averagePrice,
        BigDecimal manualValue,
        @Size(max = 500) String memo,
        @Size(max = 200) String address,
        @Size(max = 20) String dong,
        @Size(max = 20) String ho,
        @Size(max = 10) String lawdCd,
        @Size(max = 100) String complexName,
        @Size(max = 50) String regionDongName) {
}
