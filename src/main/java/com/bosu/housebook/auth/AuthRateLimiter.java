package com.bosu.housebook.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * IP당 로그인/회원가입 시도 횟수를 고정 윈도우로 제한한다. 단일 인스턴스 운영이라 인메모리로 충분하다.
 */
@Component
public class AuthRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private record Window(AtomicInteger count, Instant resetAt) {
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryConsume(String key) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.resetAt())) {
                return new Window(new AtomicInteger(1), now.plus(WINDOW));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        return window.count().get() <= MAX_ATTEMPTS;
    }

    @Scheduled(fixedRate = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void evictExpired() {
        Instant now = Instant.now();
        windows.entrySet().removeIf(entry -> now.isAfter(entry.getValue().resetAt()));
    }
}
