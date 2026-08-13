package com.bosu.housebook.category;

import com.bosu.housebook.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 특정 카테고리의 특정 달에 남긴 메모. 통계 화면에서 금액을 눌러 남긴다. */
@Entity
@Table(name = "category_memos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryMemo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false, length = 500)
    private String memo;

    public CategoryMemo(Category category, int year, int month, String memo) {
        this.category = category;
        this.year = year;
        this.month = month;
        this.memo = memo;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}
