package com.bosu.housebook.user;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.user.dto.AccountDeleteRequest;
import com.bosu.housebook.user.dto.PasswordChangeRequest;
import com.bosu.housebook.user.dto.UserResponse;
import com.bosu.housebook.user.dto.UserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMe(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateRequest request) {
        User user = getUser(userId);
        user.updateName(request.name());
        user.updateBirthDate(request.birthDate());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw ApiException.unauthorized("현재 비밀번호가 올바르지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteMe(Long userId, AccountDeleteRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw ApiException.unauthorized("비밀번호가 올바르지 않습니다.");
        }
        userRepository.delete(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
    }
}
