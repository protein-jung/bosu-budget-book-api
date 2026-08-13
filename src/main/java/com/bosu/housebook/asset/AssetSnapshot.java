package com.bosu.housebook.asset;

import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.household.Household;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 가계부(household)별로 하루 한 번(자정, Asia/Seoul) 찍는 총자산 스냅샷. byTypeJson은
 * [{"type":"REAL_ESTATE","amount":123}, ...] 형태의 원시 JSON 문자열로 저장한다 — 과거 기록이라
 * 이후 스키마가 바뀌어도 영향받지 않게 그대로 보관하고, 조회 시 그대로 파싱해서 보여준다. */
@Entity
@Table(name = "asset_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_value", nullable = false, precision = 16, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "by_type_json")
    private String byTypeJson;

    public AssetSnapshot(Household household, LocalDate snapshotDate, BigDecimal totalValue, String byTypeJson) {
        this.household = household;
        this.snapshotDate = snapshotDate;
        this.totalValue = totalValue;
        this.byTypeJson = byTypeJson;
    }

    public void update(BigDecimal totalValue, String byTypeJson) {
        this.totalValue = totalValue;
        this.byTypeJson = byTypeJson;
    }
}
