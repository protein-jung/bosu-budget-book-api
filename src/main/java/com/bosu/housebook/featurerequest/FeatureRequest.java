package com.bosu.housebook.featurerequest;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설정 &gt; 기능 요청 게시판의 글 하나. 가계부가 아니라 앱 전체가 공유하는 글타래라 household에
 * 속하지 않는다. 관리자가 다는 답변은 스레드형 댓글이 아니라 글 하나에 답변 하나만 붙는 단순한
 * 문의/답변 형태다.
 */
@Entity
@Table(name = "feature_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeatureRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "admin_reply", columnDefinition = "text")
    private String adminReply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    public FeatureRequest(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public void reply(String adminReply) {
        this.adminReply = adminReply;
        this.repliedAt = LocalDateTime.now();
    }
}
