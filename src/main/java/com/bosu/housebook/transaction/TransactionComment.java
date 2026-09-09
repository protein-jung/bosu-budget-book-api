package com.bosu.housebook.transaction;

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

/** 캘린더 거래 상세에서 가계부 구성원끼리 남기는 댓글. 누가 남겼는지 같이 보여준다. */
@Entity
@Table(name = "transaction_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String body;

    public TransactionComment(Transaction transaction, User user, String body) {
        this.transaction = transaction;
        this.user = user;
        this.body = body;
    }
}
