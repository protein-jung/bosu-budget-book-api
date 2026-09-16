package com.bosu.housebook.review;

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

/**
 * 설정 &gt; 리뷰 남기기에서 작성한 별점+한줄평 하나. 앱 전체에 대한 리뷰라 household에 속하지
 * 않고, 개수 제한 없이 언제든 다시 남길 수 있어 매번 새 행으로 쌓인다.
 */
@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "text")
    private String content;

    public Review(User user, Integer rating, String content) {
        this.user = user;
        this.rating = rating;
        this.content = content;
    }
}
