package com.bosu.housebook.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.bosu.housebook.config.JwtProperties;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(
            new JwtProperties("test-only-secret-key-at-least-32-bytes-long!!", 60_000));

    @Test
    void userTokenCarriesUserIdButNotAdminByDefault() {
        String token = provider.generateToken(42L, false);

        assertThat(provider.isAdmin(token)).isFalse();
        assertThat(provider.isUserAdmin(token)).isFalse();
        assertThat(provider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    void userTokenCanBeFlaggedAsAdminEmailWithoutBecomingTheOperatorRole() {
        String token = provider.generateToken(7L, true);

        // admin.user-email로 지정된 일반 유저 토큰은 여전히 role=USER(주체=userId)라서
        // isAdmin()(=하드코딩된 운영자 계정 전용 role=ADMIN)은 false지만, isUserAdmin()은 true.
        assertThat(provider.isAdmin(token)).isFalse();
        assertThat(provider.isUserAdmin(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo(7L);
    }

    @Test
    void adminOperatorTokenIsNotAUserAdminToken() {
        String token = provider.generateAdminToken();

        assertThat(provider.isAdmin(token)).isTrue();
        assertThat(provider.isUserAdmin(token)).isFalse();
    }
}
