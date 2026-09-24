package com.bosu.housebook.analytics;

import com.bosu.housebook.common.BaseTimeEntity;
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

/** 헤더 검색창에 타이핑한 검색어 로그 한 건. 어드민 "접속 통계" 화면에서 어떤 검색어가
 * 많이 쓰이는지 집계할 때 쓴다. 날짜만으로 검색한 경우(검색어 없음)는 기록하지 않는다. */
@Entity
@Table(name = "search_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String query;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public SearchLog(String query, User user) {
        this.query = query;
        this.user = user;
    }
}
