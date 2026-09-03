package com.bosu.housebook.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            if (jwtTokenProvider.isValid(token)) {
                if (jwtTokenProvider.isAdmin(token)) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + JwtTokenProvider.ROLE_ADMIN));
                    var authentication = new UsernamePasswordAuthenticationToken("admin", null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    Long userId = jwtTokenProvider.getUserId(token);
                    // admin.user-email로 지정된 계정의 토큰은 일반 사용자 인증(principal=userId)을
                    // 유지하면서 ROLE_ADMIN 권한도 함께 부여한다 — 이 계정으로 /api/admin/** 관리자
                    // API와 일반 API를 같은 토큰으로 모두 쓸 수 있게 하기 위함.
                    var authorities = jwtTokenProvider.isUserAdmin(token)
                            ? List.of(new SimpleGrantedAuthority("ROLE_" + JwtTokenProvider.ROLE_ADMIN))
                            : List.<SimpleGrantedAuthority>of();
                    var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
