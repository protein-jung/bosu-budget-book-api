package com.bosu.housebook.asset;

import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.household.Household;
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

    public Asset(Household household, AssetType type, String name, String custodian, String symbol,
            BigDecimal quantity, BigDecimal averagePrice, BigDecimal manualValue, String memo, String address,
            String dong, String ho) {
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
    }

    public void update(AssetType type, String name, String custodian, String symbol, BigDecimal quantity,
            BigDecimal averagePrice, BigDecimal manualValue, String memo, String address, String dong, String ho) {
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
    }

    public void updatePrice(BigDecimal price, LocalDateTime updatedAt) {
        this.currentPrice = price;
        this.priceUpdatedAt = updatedAt;
    }

    /** STOCK/CRYPTO는 단가*수량, 그 외는 직접 입력한 평가금액. 시세를 아직 못 받아왔으면 null. */
    public BigDecimal getCurrentValue() {
        if (type.isLivePriced()) {
            return currentPrice != null && quantity != null ? currentPrice.multiply(quantity) : null;
        }
        return manualValue;
    }
}
