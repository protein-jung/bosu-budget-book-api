package com.bosu.housebook.analytics;

import com.bosu.housebook.analytics.dto.ClientErrorRequest;
import com.bosu.housebook.common.SlackErrorNotifier;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 프론트에서 서버 응답을 아예 받지 못한 API 실패(네트워크 끊김·타임아웃 등)를 보고받아 슬랙으로
 * 넘긴다. 5xx는 백엔드가 이미 직접 보내므로 프론트는 응답 없는 실패만 보낸다. 웹훅 주소를 앱
 * 번들에 노출하지 않으려고 백엔드를 거친다. 로그인 전 화면에서도 나야 하므로 permitAll 경로다. */
@RestController
@RequestMapping("/api/analytics")
public class ClientErrorController {

    private final SlackErrorNotifier slackErrorNotifier;

    public ClientErrorController(SlackErrorNotifier slackErrorNotifier) {
        this.slackErrorNotifier = slackErrorNotifier;
    }

    @PostMapping("/client-error")
    public ResponseEntity<Void> report(@Valid @RequestBody ClientErrorRequest request) {
        slackErrorNotifier.notifyClientError(request, currentUserIdOrNull());
        return ResponseEntity.noContent().build();
    }

    private Long currentUserIdOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Long userId ? userId : null;
    }
}
