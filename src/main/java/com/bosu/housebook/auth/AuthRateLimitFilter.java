package com.bosu.housebook.auth;

import com.bosu.housebook.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로그인/회원가입/비밀번호 재설정처럼 무차별 대입 공격 대상이 될 수 있는 엔드포인트를 IP 기준으로 제한한다.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login", "/api/auth/signup", "/api/auth/forgot-password", "/api/auth/reset-password");

    private final AuthRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public AuthRateLimitFilter(AuthRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (LIMITED_PATHS.contains(request.getRequestURI()) && !rateLimiter.tryConsume(clientIp(request))) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write(objectMapper.writeValueAsString(new ErrorResponse("시도 횟수가 많습니다. 잠시 후 다시 시도해주세요.")));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        // Caddy가 실제 접속 IP를 체인 맨 뒤에 덧붙이므로 마지막 값만 신뢰한다.
        String[] parts = forwardedFor.split(",");
        return parts[parts.length - 1].trim();
    }
}
