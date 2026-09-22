package com.bosu.housebook.push;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.push.dto.PushTokenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push-tokens")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    public PushTokenController(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@CurrentUserId Long userId, @Valid @RequestBody PushTokenRequest request) {
        pushTokenService.register(userId, request.token());
        return ResponseEntity.noContent().build();
    }

    /** 로그아웃 시 이 기기로는 더 이상 알림이 안 가도록 토큰을 지운다. */
    @DeleteMapping
    public ResponseEntity<Void> unregister(@Valid @RequestBody PushTokenRequest request) {
        pushTokenService.unregister(request.token());
        return ResponseEntity.noContent().build();
    }
}
