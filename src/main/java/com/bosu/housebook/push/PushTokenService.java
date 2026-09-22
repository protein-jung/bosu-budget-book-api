package com.bosu.housebook.push;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    public PushTokenService(PushTokenRepository pushTokenRepository, UserRepository userRepository) {
        this.pushTokenRepository = pushTokenRepository;
        this.userRepository = userRepository;
    }

    /** 같은 토큰이 이미 등록돼있으면(기기 재로그인 등) 소유자만 갱신하고, 없으면 새로 만든다. */
    @Transactional
    public void register(Long userId, String token) {
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        var existing = pushTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            existing.get().reassignTo(user);
        } else {
            pushTokenRepository.save(new PushToken(user, token));
        }
    }

    @Transactional
    public void unregister(String token) {
        pushTokenRepository.deleteByToken(token);
    }
}
