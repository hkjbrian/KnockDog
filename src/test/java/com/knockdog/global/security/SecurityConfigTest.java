package com.knockdog.global.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.knockdog.AuthApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void 권한부족_요청은_JSON_403을_반환한다() throws Exception {
        mockMvc().perform(get("/api/_test/admin-only").with(user("u").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    void 루트경로는_인증없이_접근가능하다() throws Exception {
        // permitAll이므로 인증 실패(401)가 아니어야 한다. 라우트 미존재(404)나 정적/기본 응답은 허용.
        mockMvc().perform(get("/"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("permitAll 경로가 인증/인가 실패로 응답됨: " + status);
                    }
                });
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
