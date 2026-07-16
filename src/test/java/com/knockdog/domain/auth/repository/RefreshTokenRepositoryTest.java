package com.knockdog.domain.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.knockdog.domain.auth.entity.RefreshToken;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired private RefreshTokenRepository repository;

    private RefreshToken sample(Long userId, String token) {
        return RefreshToken.create(userId, token, Instant.now().plus(14, ChronoUnit.DAYS));
    }

    @Test
    void 저장한_refresh_토큰을_token으로_조회한다() {
        repository.save(sample(42L, "refresh-abc"));

        Optional<RefreshToken> found = repository.findByToken("refresh-abc");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(42L);
    }

    @Test
    void 로그아웃시_userId의_refresh_토큰을_삭제한다() {
        repository.save(sample(42L, "refresh-abc"));

        repository.deleteByUserId(42L);

        assertThat(repository.findByToken("refresh-abc")).isEmpty();
    }
}
