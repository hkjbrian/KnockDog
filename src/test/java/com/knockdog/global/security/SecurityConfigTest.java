package com.knockdog.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.knockdog.AuthApplication;
import com.knockdog.domain.auth.service.AuthService;
import com.knockdog.domain.user.entity.Role;
import com.knockdog.domain.user.entity.User;
import com.knockdog.domain.user.repository.UserRepository;
import com.knockdog.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * SecurityConfig 통합 테스트.
 *
 * <p>테스트 전용 컨트롤러/설정을 {@link TestConfiguration}으로 주입해 main 코드에 임시 라우트를
 * 남기지 않는다. Spring Security 필터가 실제로 적용되도록 {@code MockMvcBuilders#webAppContextSetup}
 * + {@code springSecurity()}를 사용한다.
 */
@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
@Import(SecurityConfigTest.TestSecurityBeans.class)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void 미인증_API_요청은_JSON_401을_반환한다() throws Exception {
        mockMvc().perform(get("/api/foo"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 권한부족_요청은_JSON_403을_반환한다() throws Exception {
        mockMvc().perform(get("/api/_test/admin-only").with(user("u").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 루트경로에서_인증_확인_정적_화면을_제공한다() throws Exception {
        mockMvc().perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc().perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/oauth2/authorization/kakao")))
                .andExpect(content().string(containsString("<pre id=\"status\">")))
                .andExpect(content().string(containsString("Authorization: Bearer")))
                .andExpect(content().string(containsString("/api/auth/csrf")));
    }

    @Test
    void Authorization_Bearer_헤더의_JWT로_API를_인증한다() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(1L, null);

        mockMvc().perform(get("/api/foo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("foo"));
    }

    @Test
    void Authorization_헤더없이_쿠키만으로는_API를_인증할_수_없다() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(1L, null);

        mockMvc().perform(get("/api/foo").cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void API_상태변경_요청은_CSRF_토큰없이_차단된다() throws Exception {
        mockMvc().perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void Bearer_헤더와_CSRF_토큰으로_역할을_확정하고_access_응답과_refresh_쿠키를_설정한다() throws Exception {
        User user = authService.findOrCreateKakaoUser(3001L, "댕댕이");
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), null);

        mockMvc().perform(post("/api/auth/signup/role")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"OWNER\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("KD_REFRESH=")))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("역할이 확정되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").isString());

        assertThat(userRepository.findById(user.getId()).orElseThrow().getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    void refresh_쿠키와_CSRF_토큰으로_access_토큰을_재발급한다() throws Exception {
        User user = authService.findOrCreateKakaoUser(3002L, "초코");
        String refreshToken = authService.issueTokens(user.getId()).refreshToken();

        mockMvc().perform(post("/api/auth/refresh")
                        .cookie(new Cookie("KD_REFRESH", refreshToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("KD_REFRESH=")))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    void 브라우저_로그아웃은_CSRF_토큰없이_차단된다() throws Exception {
        mockMvc().perform(post("/logout"))
                .andExpect(status().isForbidden());

        mockMvc().perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * 테스트 전용 컨트롤러: 인가 실패(403) 케이스를 유도하기 위한 보호 엔드포인트.
     * main 스코프에 임시 라우트를 남기지 않기 위해 test 스코프의 {@link TestConfiguration}로만 노출한다.
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityBeans {

        @Bean
        TestSecuredController testSecuredController() {
            return new TestSecuredController();
        }
    }

    @RestController
    static class TestSecuredController {

        @GetMapping("/api/_test/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminOnly() {
            return "ok";
        }

        @GetMapping("/api/foo")
        public String foo() {
            return "foo";
        }
    }
}
