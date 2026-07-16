package com.knockdog.domain.auth.service;

import com.knockdog.domain.user.entity.User;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/** 카카오 사용자 정보의 ID·닉네임만 받아 로컬 사용자를 조회하거나 최초 가입시킨다. */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final AuthService authService;

    /** 카카오 응답을 최소 사용자 정보로 변환해 로컬 principal을 만든다. */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = getKakaoId(attributes);
        String nickname = getNickname(attributes);
        User user = authService.findOrCreateKakaoUser(kakaoId, nickname);
        return new KakaoOAuth2User(user.getId(), attributes);
    }

    private Long getKakaoId(Map<String, Object> attributes) {
        Object value = attributes.get("id");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Long.valueOf(stringValue);
            } catch (NumberFormatException exception) {
                throw invalidKakaoResponse();
            }
        }
        throw invalidKakaoResponse();
    }

    @SuppressWarnings("unchecked")
    private String getNickname(Map<String, Object> attributes) {
        Object properties = attributes.get("properties");
        if (!(properties instanceof Map<?, ?> propertyMap)) {
            throw invalidKakaoResponse();
        }
        Object nickname = ((Map<String, Object>) propertyMap).get("nickname");
        if (!(nickname instanceof String nicknameValue) || nicknameValue.isBlank()) {
            throw invalidKakaoResponse();
        }
        return nicknameValue;
    }

    private OAuth2AuthenticationException invalidKakaoResponse() {
        return new OAuth2AuthenticationException("invalid_kakao_user_response");
    }
}
