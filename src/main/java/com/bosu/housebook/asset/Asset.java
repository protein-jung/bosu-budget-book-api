package com.bosu.housebook.asset;

import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(nullable = false)
    private String name;

    /** 은행/증권사/거래소 등 보관처. 자유 입력. */
    private String custodian;

    /** STOCK/CRYPTO 전용: 티커(005930.KS, AAPL) 또는 코인 심볼(BTC). */
    private String symbol;

    /** STOCK/CRYPTO 전용: 보유 수량. */
    @Column(precision = 20, scale = 8)
    private BigDecimal quantity;

    /** STOCK/CRYPTO 전용(선택): 매수 평단가(원). */
    @Column(name = "average_price", precision = 20, scale = 4)
    private BigDecimal averagePrice;

    /** 그 외 자산 전용: 직접 입력한 평가금액(원). */
    @Column(name = "manual_value", precision = 16, scale = 2)
    private BigDecimal manualValue;

    /** STOCK/CRYPTO 전용: 마지막으로 조회된 원화 단가. */
    @Column(name = "current_price", precision = 20, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;

    private String memo;

    /** 부동산 전용(선택): 주소/동/호수. 다른 타입도 막지는 않는다 — 그냥 안 쓸 뿐. */
    private String address;

    private String dong;

    private String ho;

    /** 부동산 전용(선택): 국토부 실거래가 자동 조회에 쓰는 법정동 코드/단지명/법정동명. */
    @Column(name = "lawd_cd")
    private String lawdCd;

    @Column(name = "complex_name")
    private String complexName;

    @Column(name = "region_dong_name")
    private String regionDongName;

    /** STOCK 전용(선택): 일반 계좌 보유인지 연금(IRP/연금저축) 계좌 보유인지. 기본값 GENERAL. */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_category", nullable = false)
    private AccountCategory accountCategory = AccountCategory.GENERAL;

    /** 부동산/차량 외 자산 전용(선택): 소유주(가계부 구성원). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    public Asset(Household household, AssetType type, String name, String custodian, String symbol,
            BigDecimal quantity, BigDecimal averagePrice, BigDecimal manualValue, String memo, String address,
            String dong, String ho, String lawdCd, String complexName, String regionDongName,
            AccountCategory accountCategory, User owner) {
        this.household = household;
        this.type = type;
        this.name = name;
        this.custodian = custodian;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.manualValue = manualValue;
        this.memo = memo;
        this.address = address;
        this.dong = dong;
        this.ho = ho;
        this.lawdCd = lawdCd;
        this.complexName = complexName;
        this.regionDongName = regionDongName;
        this.accountCategory = accountCategory != null ? accountCategory : AccountCategory.GENERAL;
        this.owner = owner;
    }

    public void update(AssetType type, String name, String custodian, String symbol, BigDecimal quantity,
            BigDecimal averagePrice, BigDecimal manualValue, String memo, String address, String dong, String ho,
            String lawdCd, String complexName, String regionDongName, AccountCategory accountCategory) {
        this.type = type;
        this.name = name;
        this.custodian = custodian;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.manualValue = manualValue;
        this.memo = memo;
        this.address = address;
        this.dong = dong;
        this.ho = ho;
        this.lawdCd = lawdCd;
        this.complexName = complexName;
        this.regionDongName = regionDongName;
        this.accountCategory = accountCategory != null ? accountCategory : AccountCategory.GENERAL;
    }

    public void updatePrice(BigDecimal price, LocalDateTime updatedAt) {
        this.currentPrice = price;
        this.priceUpdatedAt = updatedAt;
    }

    /** REAL_ESTATE는 국토부 실거래가(있으면)를 우선하고 없으면 직접 입력값, STOCK/CRYPTO는 단가*수량,
     * 그 외는 직접 입력한 평가금액. 시세를 아직 못 받아왔으면 null. */
    public BigDecimal getCurrentValue() {
        if (type == AssetType.REAL_ESTATE) {
            return currentPrice != null ? currentPrice : manualValue;
        }
        if (type.isLivePriced()) {
            return currentPrice != null && quantity != null ? currentPrice.multiply(quantity) : null;
        }
        return manualValue;
    }
}
