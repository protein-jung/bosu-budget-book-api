package com.bosu.housebook.asset.dto;

import com.bosu.housebook.asset.AccountCategory;
import com.bosu.housebook.asset.Asset;
import com.bosu.housebook.asset.AssetType;
import com.bosu.housebook.asset.CashCategory;
import com.bosu.housebook.asset.LoanRepaymentType;
import com.bosu.housebook.asset.RealEstateCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
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
        String ownerName,
        CashCategory cashCategory,
        LocalDate maturityDate,
        boolean matured,
        BigDecimal cashInterestRate,
        LocalDate cashStartDate,
        LocalDate purchaseDate,
        String encarUrl,
        BigDecimal loanPrincipal,
        LocalDate loanStartMonth,
        Integer loanTermMonths,
        BigDecimal loanMonthlyPayment,
        BigDecimal loanInterestRate,
        LoanRepaymentType loanRepaymentType,
        BigDecimal currentMonthlyPayment,
        RealEstateCategory realEstateCategory,
        BigDecimal monthlyRent) {

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
                asset.getOwner() != null ? asset.getOwner().getName() : null,
                asset.getCashCategory(),
                asset.getMaturityDate(),
                asset.isMatured(),
                asset.getCashInterestRate(),
                asset.getCashStartDate(),
                asset.getPurchaseDate(),
                asset.getEncarUrl(),
                asset.getLoanPrincipal(),
                asset.getLoanStartMonth(),
                asset.getLoanTermMonths(),
                asset.getLoanMonthlyPayment(),
                asset.getLoanInterestRate(),
                asset.getLoanRepaymentType(),
                asset.getCurrentMonthlyPayment(),
                asset.getRealEstateCategory(),
                asset.getMonthlyRent());
    }
}
