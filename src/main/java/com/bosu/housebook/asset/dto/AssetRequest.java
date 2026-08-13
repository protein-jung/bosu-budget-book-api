package com.bosu.housebook.asset.dto;

import com.bosu.housebook.asset.AccountCategory;
import com.bosu.housebook.asset.AssetType;
import com.bosu.housebook.asset.CashCategory;
import com.bosu.housebook.asset.LoanRepaymentType;
import com.bosu.housebook.asset.PriceCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * type이 STOCK/CRYPTO이고 symbol이 있으면 quantity가, 그 외(심볼 없는 STOCK/CRYPTO 포함)면
 * manualValue가 있어야 한다 — 조건부라 여기서는 형식(@Size 등)만 검증하고 필수 여부는
 * AssetService에서 확인한다. averagePriceCurrency는 STOCK의 averagePrice를 어떤 통화로
 * 입력했는지 나타내며, USD면 AssetService가 저장 시점에 원화로 환산해 averagePrice에 넣는다
 * (별도로 영속되지 않음). loan* 필드는 LOAN 전용이며 전부 있어야 상환 스케줄을 계산한다.
 */
public record AssetRequest(
        @NotNull AssetType type,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String custodian,
        @Size(max = 20) String symbol,
        BigDecimal quantity,
        BigDecimal averagePrice,
        PriceCurrency averagePriceCurrency,
        BigDecimal manualValue,
        @Size(max = 500) String memo,
        @Size(max = 200) String address,
        @Size(max = 20) String dong,
        @Size(max = 20) String ho,
        @Size(max = 10) String lawdCd,
        @Size(max = 100) String complexName,
        @Size(max = 50) String regionDongName,
        AccountCategory accountCategory,
        CashCategory cashCategory,
        LocalDate maturityDate,
        BigDecimal cashInterestRate,
        LocalDate cashStartDate,
        LocalDate purchaseDate,
        @Size(max = 2000) String encarUrl,
        BigDecimal loanPrincipal,
        LocalDate loanStartMonth,
        Integer loanTermMonths,
        BigDecimal loanMonthlyPayment,
        BigDecimal loanInterestRate,
        LoanRepaymentType loanRepaymentType) {
}
