package com.bosu.housebook.category;

import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.common.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가계부 생성 시 시드할 기본 카테고리 트리(모든 가계부가 공유하는 공통 템플릿). 실제 시드는
 * {@link CategoryDefaultSeeder}가 이 테이블을 읽어 household 소유의 {@link Category} 행으로
 * 복제한다. parentId가 null이면 최상위(그룹 또는 단독 리프), 아니면 그 그룹의 하위 소분류다.
 */
@Entity
@Table(name = "default_category_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCategoryTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String name;

    private String color;

    private String icon;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "is_group", nullable = false)
    private boolean isGroup;

    @Column(name = "excluded_from_expense_stats", nullable = false)
    private boolean excludedFromExpenseStats;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
