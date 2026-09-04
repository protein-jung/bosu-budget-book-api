package com.bosu.housebook.auth;

import com.bosu.housebook.auth.dto.LoginRequest;
import com.bosu.housebook.auth.dto.SignupRequest;
import com.bosu.housebook.auth.dto.TokenResponse;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.config.AdminProperties;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final int MIN_SIGNUP_AGE = 14;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminProperties adminProperties;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider, AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminProperties = adminProperties;
    }

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");
        }
        if (Period.between(request.birthDate(), LocalDate.now()).getYears() < MIN_SIGNUP_AGE) {
            throw ApiException.badRequest("만 14세 미만은 가입할 수 없습니다.");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.name(),
                request.birthDate());
        userRepository.save(user);
        return toTokenResponse(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (user.isBlocked()) {
            throw ApiException.forbidden("차단된 계정입니다. 관리자에게 문의해주세요.");
        }
        return toTokenResponse(user);
    }

    private TokenResponse toTokenResponse(User user) {
        boolean isAdmin = isAdminEmail(user.getEmail());
        String token = jwtTokenProvider.generateToken(user.getId(), isAdmin);
        return new TokenResponse(token, user.getId(), user.getEmail(), user.getName(), user.getBirthDate(), isAdmin);
    }

    private boolean isAdminEmail(String email) {
        String adminEmail = adminProperties.userEmail();
        return adminEmail != null && !adminEmail.isBlank() && adminEmail.equalsIgnoreCase(email);
    }
}
