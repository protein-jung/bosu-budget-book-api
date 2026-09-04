package com.bosu.housebook.auth;

import com.bosu.housebook.auth.dto.ForgotPasswordRequest;
import com.bosu.housebook.auth.dto.ResetPasswordRequest;
import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.featurerequest.MailService;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    // 이메일 존재 여부와 무관하게 항상 같은 응답을 준다(가입 여부로 계정 존재를 유추하지 못하게).
    // 그래서 이 메서드는 예외를 던지지 않고, 계정이 있을 때만 조용히 메일을 보낸다.
    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = generateToken();
            tokenRepository.save(
                    new PasswordResetToken(user.getId(), token, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));
            String link = frontendUrl + "/reset-password?token=" + token;
            mailService.send(user.getEmail(), "[보수가계부] 비밀번호 재설정",
                    "비밀번호를 재설정하려면 아래 링크를 눌러주세요 (30분간 유효):\n\n" + link
                            + "\n\n본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.");
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .filter(PasswordResetToken::isValid)
                .orElseThrow(() -> ApiException.badRequest("유효하지 않거나 만료된 링크입니다."));
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> ApiException.badRequest("유효하지 않거나 만료된 링크입니다."));
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        resetToken.markUsed();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
