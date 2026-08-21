package com.bosu.housebook.recurringexpense;

import com.bosu.housebook.category.Category;
import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.household.Household;
import com.bosu.housebook.user.User;
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

/** 매달 정해진 날짜에 자동으로 거래를 생성하는 고정비 지출(구독료, 월세 등) 정의. 실제 생성은
 * {@link RecurringExpenseGenerationService}가 매일 배치로 처리한다. */
@Entity
@Table(name = "recurring_expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringExpense extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /** 매달 이 날짜에 자동 생성된다. 실제 일수가 모자란 달(예: 2월에 30일)에는 그 달의 말일로 맞춘다. */
    @Column(name = "day_of_month", nullable = false)
    private int dayOfMonth;

    @Column(nullable = false)
    private boolean active;

    private String memo;

    /** 이번 달에 이미 생성했는지 판단하는 기준. 배치가 이 값의 연-월과 오늘의 연-월이 같으면 건너뛴다. */
    @Column(name = "last_generated_date")
    private LocalDate lastGeneratedDate;

    public RecurringExpense(Household household, Category category, User createdBy, String name, BigDecimal amount,
            int dayOfMonth, boolean active, String memo) {
        this.household = household;
        this.category = category;
        this.createdBy = createdBy;
        this.name = name;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
        this.active = active;
        this.memo = memo;
    }

    public void update(Category category, String name, BigDecimal amount, int dayOfMonth, boolean active,
            String memo) {
        this.category = category;
        this.name = name;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
        this.active = active;
        this.memo = memo;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }

    public void markGenerated(LocalDate date) {
        this.lastGeneratedDate = date;
    }
}
