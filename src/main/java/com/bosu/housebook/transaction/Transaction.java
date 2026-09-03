package com.bosu.housebook.transaction;

import com.bosu.housebook.card.Card;
import com.bosu.housebook.category.Category;
import com.bosu.housebook.common.BaseTimeEntity;
import com.bosu.housebook.common.TransactionType;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String memo;

    // 제목(memo)과 별개로 남기는 자유 메모. 예: "다음 달엔 이 카드 대신 다른 카드로".
    private String note;

    // 엑셀 등에서 가져온 월별 합계처럼 정확한 날짜가 없는 과거 데이터에 붙는 표시. 통계(월별
    // 합계)에는 그대로 포함되지만 캘린더 일별 목록에서는 제외된다(TransactionRepository의
    // ...AndIsBackfillFalse... 조회 참고). 새로 만드는 거래는 항상 false.
    @Column(name = "is_backfill", nullable = false)
    private boolean isBackfill;

    public Transaction(Household household, TransactionType type, BigDecimal amount, LocalDate transactionDate,
            Category category, Card card, User user, String memo) {
        this(household, type, amount, transactionDate, category, card, user, memo, null);
    }

    public Transaction(Household household, TransactionType type, BigDecimal amount, LocalDate transactionDate,
            Category category, Card card, User user, String memo, String note) {
        this.household = household;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.category = category;
        this.card = card;
        this.user = user;
        this.memo = memo;
        this.note = note;
    }

    public void update(TransactionType type, BigDecimal amount, LocalDate transactionDate, Category category,
            Card card, String memo, String note) {
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.category = category;
        this.card = card;
        this.memo = memo;
        this.note = note;
    }
}
