package com.bosu.housebook.analytics;

import com.bosu.housebook.analytics.dto.PageViewRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 여부와 무관하게 프론트의 모든 페이지(웰컴·로그인·회원가입 포함)에서 호출하는
 * 방문 기록 API. 인증이 필요 없는 permitAll 엔드포인트다 — 그래도 요청에 유효한 토큰이
 * 실려 있으면 JwtAuthFilter가 SecurityContext를 채워주므로, 로그인한 사용자의 방문은
 * userId까지 같이 기록된다. */
@RestController
@RequestMapping("/api/analytics")
public class PageViewController {

    private final PageViewService pageViewService;

    public PageViewController(PageViewService pageViewService) {
        this.pageViewService = pageViewService;
    }

    @PostMapping("/pageview")
    public ResponseEntity<Void> pageview(@Valid @RequestBody PageViewRequest request) {
        pageViewService.record(request, currentUserIdOrNull());
        return ResponseEntity.noContent().build();
    }

    private Long currentUserIdOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Long userId ? userId : null;
    }
}
