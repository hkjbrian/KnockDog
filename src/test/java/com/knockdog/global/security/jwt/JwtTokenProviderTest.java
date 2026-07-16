package com.knockdog.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.knockdog.domain.user.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-for-hs256-must-be-at-least-32-bytes-long-000";

    private JwtTokenProvider provider() {
        return provider(SECRET, Clock.systemUTC());
    }

    private JwtTokenProvider provider(Clock clock) {
        return provider(SECRET, clock);
    }

    private JwtTokenProvider provider(String secret, Clock clock) {
        return new JwtTokenProvider(
                new JwtProperties(secret, Duration.ofMinutes(30), Duration.ofDays(14)), clock);
    }

    /** access 만료(30m)를 지나 이미 만료된 토큰을 만들기 위해 1시간 과거로 고정한 시계. */
    private Clock oneHourAgo() {
        return Clock.fixed(Instant.now().minus(1, ChronoUnit.HOURS), ZoneOffset.UTC);
    }

    @Test
    void access_토큰은_생성후_검증하면_userId와_role이_복원된다() {
        JwtTokenProvider provider = provider();

        String token = provider.createAccessToken(42L, Role.OWNER);

        assertThat(provider.getUserId(token)).isEqualTo(42L);
        assertThat(provider.getRole(token)).isEqualTo(Role.OWNER);
    }

    @Test
    void refresh_토큰은_생성후_검증하면_userId만_있고_role은_없다() {
        JwtTokenProvider provider = provider();

        String token = provider.createRefreshToken(42L);

        assertThat(provider.getUserId(token)).isEqualTo(42L);
        assertThat(provider.getRole(token)).isNull();
    }

    @Test
    void 온보딩_전이면_access_토큰에_role이_없다() {
        JwtTokenProvider provider = provider();

        String token = provider.createAccessToken(42L, null);

        assertThat(provider.getUserId(token)).isEqualTo(42L);
        assertThat(provider.getRole(token)).isNull();
    }

    @Test
    void validate는_유효한_토큰이면_true를_반환한다() {
        JwtTokenProvider provider = provider();

        String token = provider.createAccessToken(42L, Role.OWNER);

        assertThat(provider.validate(token)).isTrue();
    }

    @Test
    void validate는_만료된_토큰이면_false를_반환한다() {
        JwtTokenProvider provider = provider(oneHourAgo());

        String expired = provider.createAccessToken(42L, Role.OWNER);

        assertThat(provider.validate(expired)).isFalse();
    }

    @Test
    void validate는_다른_키로_서명된_토큰이면_false를_반환한다() {
        JwtTokenProvider issuer = provider();
        JwtTokenProvider verifier =
                provider("another-different-secret-key-at-least-32-bytes-long-123456", Clock.systemUTC());

        String forged = issuer.createAccessToken(42L, Role.OWNER);

        assertThat(verifier.validate(forged)).isFalse();
    }

    @Test
    void 만료된_토큰을_parse하면_ExpiredJwtException을_던진다() {
        JwtTokenProvider provider = provider(oneHourAgo());

        String expired = provider.createAccessToken(42L, Role.OWNER);

        assertThatThrownBy(() -> provider.parseClaims(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
