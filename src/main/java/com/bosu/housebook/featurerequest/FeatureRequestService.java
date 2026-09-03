package com.bosu.housebook.featurerequest;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.featurerequest.dto.AdminFeatureRequestResponse;
import com.bosu.housebook.featurerequest.dto.FeatureRequestCreateRequest;
import com.bosu.housebook.featurerequest.dto.FeatureRequestReplyRequest;
import com.bosu.housebook.featurerequest.dto.FeatureRequestResponse;
import com.bosu.housebook.notification.NotificationService;
import com.bosu.housebook.notification.NotificationType;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 설정 &gt; 기능 요청 게시판. 가계부가 아니라 앱을 쓰는 모든 사람이 공유하는 하나의 글타래라
 * household로 필터링하지 않는다 — 로그인한 사용자면 누구나 전체 글을 보고 글을 쓸 수 있다. */
@Service
@Transactional(readOnly = true)
public class FeatureRequestService {

    private static final String REPLY_EMAIL_SUBJECT = "[BOSU Ledger] 남겨주신 기능 요청에 답변이 달렸어요";

    private final FeatureRequestRepository featureRequestRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final NotificationService notificationService;

    public FeatureRequestService(FeatureRequestRepository featureRequestRepository, UserRepository userRepository,
            MailService mailService, NotificationService notificationService) {
        this.featureRequestRepository = featureRequestRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.notificationService = notificationService;
    }

    public List<FeatureRequestResponse> getAll() {
        return featureRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FeatureRequestResponse::from)
                .toList();
    }

    @Transactional
    public FeatureRequestResponse create(Long userId, FeatureRequestCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        FeatureRequest saved = featureRequestRepository
                .save(new FeatureRequest(user, request.title().trim(), request.content().trim()));
        return FeatureRequestResponse.from(saved);
    }

    public List<AdminFeatureRequestResponse> getAllForAdmin() {
        return featureRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminFeatureRequestResponse::from)
                .toList();
    }

    @Transactional
    public AdminFeatureRequestResponse reply(Long requestId, FeatureRequestReplyRequest request) {
        FeatureRequest featureRequest = featureRequestRepository.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다."));
        featureRequest.reply(request.reply().trim());

        if (featureRequest.getUser() != null) {
            String body = """
                    "%s"에 남겨주신 요청에 답변이 달렸어요.

                    [남겨주신 내용]
                    %s

                    [답변]
                    %s
                    """.formatted(featureRequest.getTitle(), featureRequest.getContent(), featureRequest.getAdminReply());
            mailService.send(featureRequest.getUser().getEmail(), REPLY_EMAIL_SUBJECT, body);
            notificationService.create(featureRequest.getUser(), NotificationType.FEATURE_REQUEST_REPLY,
                    "기능 요청에 답변이 달렸어요", featureRequest.getTitle(), "/settings/feature-requests");
        }

        return AdminFeatureRequestResponse.from(featureRequest);
    }
}
