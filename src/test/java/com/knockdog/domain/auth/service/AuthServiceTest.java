package com.knockdog.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.knockdog.AuthApplication;
import com.knockdog.domain.auth.repository.RefreshTokenRepository;
import com.knockdog.domain.user.entity.Role;
import com.knockdog.domain.user.entity.User;
import com.knockdog.domain.user.repository.UserRepository;
import com.knockdog.global.exception.BusinessException;
import com.knockdog.global.exception.ErrorCode;
import com.knockdog.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void refresh는_기존토큰을_회전시키고_재사용을_거부한다() {
        User user = authService.findOrCreateKakaoUser(1001L, "멍멍이");
        TokenPair oldPair = authService.issueTokens(user.getId());

        TokenPair newPair = authService.refresh(oldPair.refreshToken());

        assertThat(newPair.accessToken()).isNotEqualTo(oldPair.accessToken());
        assertThat(newPair.refreshToken()).isNotEqualTo(oldPair.refreshToken());
        assertThat(refreshTokenRepository.findByToken(newPair.refreshToken())).isPresent();
        assertThatThrownBy(() -> authService.refresh(oldPair.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 역할을_확정하면_새_access_토큰에_role_클레임을_넣는다() {
        User user = authService.findOrCreateKakaoUser(1002L, "코코");

        TokenPair tokenPair = authService.assignRole(user.getId(), Role.OWNER);

        assertThat(userRepository.findById(user.getId()).orElseThrow().getRole()).isEqualTo(Role.OWNER);
        assertThat(jwtTokenProvider.getRole(tokenPair.accessToken())).isEqualTo(Role.OWNER);
    }
}
