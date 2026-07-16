package com.knockdog.domain.auth.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/** 카카오 응답을 DB 사용자와 연결한 OAuth 로그인 principal. */
public class KakaoOAuth2User implements OAuth2User {

    private final Long userId;
    private final Map<String, Object> attributes;

    public KakaoOAuth2User(Long userId, Map<String, Object> attributes) {
        this.userId = userId;
        this.attributes = attributes;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
