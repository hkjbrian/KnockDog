package com.knockdog.domain.home.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.knockdog.AuthApplication;
import com.knockdog.domain.user.entity.Role;
import com.knockdog.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 견주_access_JWT는_견주_홈_데이터를_반환한다() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(1L, Role.OWNER);

        mockMvc.perform(get("/api/home").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.role").value("OWNER"))
                .andExpect(jsonPath("$.data.myDogs[0]").value("초코"))
                .andExpect(jsonPath("$.data.nearbyKindergartens").isArray());
    }

    @Test
    void 원장_access_JWT는_원장_홈_데이터를_반환한다() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(2L, Role.DIRECTOR);

        mockMvc.perform(get("/api/home").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.role").value("DIRECTOR"))
                .andExpect(jsonPath("$.data.kindergartenName").value("멍멍 유치원"))
                .andExpect(jsonPath("$.data.enrolledDogs").isArray())
                .andExpect(jsonPath("$.data.todayAttendance").value(3));
    }

    @Test
    void 역할없는_access_JWT는_홈에_접근하면_403을_반환한다() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(3L, null);

        mockMvc.perform(get("/api/home").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
