package com.knockdog.domain.home.controller;

import com.knockdog.domain.home.dto.response.HomeResponse;
import com.knockdog.domain.home.service.HomeService;
import com.knockdog.domain.user.entity.Role;
import com.knockdog.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Bearer access JWT의 역할에 따라 홈 화면을 분기한다. */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /** OWNER/DIRECTOR role 클레임이 있는 사용자만 역할별 홈 데이터를 조회한다. */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'DIRECTOR')")
    public ResponseEntity<ApiResponse<HomeResponse>> home(@AuthenticationPrincipal Jwt jwt) {
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        return ResponseEntity.ok(ApiResponse.success(homeService.getHome(role)));
    }
}
