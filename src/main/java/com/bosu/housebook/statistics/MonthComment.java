package com.bosu.housebook.statistics;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 통계(월별 요약) 화면 맨 위에서 그 달에 대해 남기는 코멘트. 누가 남겼는지 같이 보여준다. */
@Entity
@Table(name = "month_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false, length = 500)
    private String body;

    public MonthComment(Household household, User user, int year, int month, String body) {
        this.household = household;
        this.user = user;
        this.year = year;
        this.month = month;
        this.body = body;
    }
}
