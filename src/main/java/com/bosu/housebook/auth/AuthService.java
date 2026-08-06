package com.bosu.housebook.auth;

import com.bosu.housebook.auth.dto.LoginRequest;
import com.bosu.housebook.auth.dto.SignupRequest;
import com.bosu.housebook.auth.dto.TokenResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.name());
        userRepository.save(user);
        return toTokenResponse(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return toTokenResponse(user);
    }

    private TokenResponse toTokenResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getId());
        return new TokenResponse(token, user.getId(), user.getEmail(), user.getName());
    }
}
