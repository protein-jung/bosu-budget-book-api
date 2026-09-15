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

/** 프론트 페이지 방문 로그 한 건. 로그인 전 페이지(웰컴·로그인·회원가입 등)도 포함해서
 * 프론트의 모든 페이지에서 기록된다. */
@Entity
@Table(name = "page_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageView extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    /** 기기/브라우저에 저장된 익명 식별자. 로그인 여부와 무관하게 항상 채워진다. */
    @Column(name = "visitor_id", nullable = false, length = 64)
    private String visitorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public PageView(String path, String visitorId, User user) {
        this.path = path;
        this.visitorId = visitorId;
        this.user = user;
    }
}
