package com.bosu.housebook.category;

import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.common.TransactionType;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String color;

    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    /** 하위 카테고리를 묶기만 하는 상위(그룹) 카테고리인지. true면 거래 입력 시 선택 대상에서
     * 빠진다 — 하위가 아직 없어도 통계/설정 화면에서 그룹으로 취급하기 위한 명시적 표시. */
    @Column(name = "is_group", nullable = false)
    private boolean isGroup;

    public Category(Household household, String name, TransactionType type, String color, String icon) {
        this(household, name, type, color, icon, null, 0, null, false);
    }

    public Category(Household household, String name, TransactionType type, String color, String icon,
            Category parent, int sortOrder) {
        this(household, name, type, color, icon, parent, sortOrder, null, false);
    }

    public Category(Household household, String name, TransactionType type, String color, String icon,
            Category parent, int sortOrder, BigDecimal targetAmount) {
        this(household, name, type, color, icon, parent, sortOrder, targetAmount, false);
    }

    public Category(Household household, String name, TransactionType type, String color, String icon,
            Category parent, int sortOrder, BigDecimal targetAmount, boolean isGroup) {
        this.household = household;
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
        this.parent = parent;
        this.sortOrder = sortOrder;
        this.targetAmount = targetAmount;
        this.isGroup = isGroup;
    }

    public void update(String name, String color, String icon, BigDecimal targetAmount, boolean isGroup) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.targetAmount = targetAmount;
        this.isGroup = isGroup;
    }

    public void updateParent(Category parent) {
        this.parent = parent;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
