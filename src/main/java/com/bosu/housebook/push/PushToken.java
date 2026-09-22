package com.bosu.housebook.push;

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

/** 앱 푸시 알림을 보낼 기기의 Expo 푸시 토큰. 한 사용자가 여러 기기를 쓸 수 있어 사용자당
 * 여러 개가 있을 수 있다. */
@Entity
@Table(name = "push_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 200)
    private String token;

    public PushToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public void reassignTo(User user) {
        this.user = user;
    }
}
