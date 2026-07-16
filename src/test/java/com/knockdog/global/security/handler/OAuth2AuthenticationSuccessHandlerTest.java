package com.knockdog.global.security.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.knockdog.AuthApplication;
import com.knockdog.domain.auth.service.AuthService;
import com.knockdog.domain.auth.service.KakaoOAuth2User;
import com.knockdog.domain.user.entity.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

/** OAuth 성공 시 Access 쿠키 없이 refresh 쿠키와 로그인 완료 리다이렉트를 설정하는지 검증한다. */
@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
class OAuth2AuthenticationSuccessHandlerTest {

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthService authService;

    @Test
    void OAuth_성공시_refresh_쿠키만_설정하고_로그인완료_프론트로_리다이렉트한다() throws Exception {
        User user = authService.findOrCreateKakaoUser(4001L, "보리");
        KakaoOAuth2User principal = new KakaoOAuth2User(user.getId(), Map.of());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).contains("login=success");
        assertThat(response.getHeaders("Set-Cookie"))
                .singleElement()
                .matches(cookie -> cookie.startsWith("KD_REFRESH="));
    }
}
