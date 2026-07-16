package com.knockdog.domain.auth.service;

import com.knockdog.domain.auth.entity.RefreshToken;
import com.knockdog.domain.auth.repository.RefreshTokenRepository;
import com.knockdog.domain.user.entity.Role;
import com.knockdog.domain.user.entity.User;
import com.knockdog.domain.user.repository.UserRepository;
import com.knockdog.global.exception.BusinessException;
import com.knockdog.global.exception.ErrorCode;
import com.knockdog.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 카카오 가입, 역할 온보딩, refresh 회전과 로그아웃을 처리한다. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Clock clock;

    /** 카카오 ID로 기존 사용자를 찾거나, 없으면 닉네임만 저장해 가입시킨다. */
    @Transactional
    public User findOrCreateKakaoUser(Long kakaoId, String nickname) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(User.create(kakaoId, nickname, clock.instant())));
    }

    /** OAuth 성공 후 사용자의 role을 반영한 access/refresh 토큰을 발급한다. */
    @Transactional
    public TokenPair issueTokens(Long userId) {
        User user = getUser(userId);
        return replaceTokens(user);
    }

    /** 온보딩 역할을 확정하고 role 클레임을 포함한 새 토큰을 발급한다. */
    @Transactional
    public TokenPair assignRole(Long userId, Role role) {
        User user = getUser(userId);
        user.assignRole(role, clock.instant());
        return replaceTokens(user);
    }

    /** DB에 저장된 refresh 토큰만 검증해 한 번 사용 가능한 새 토큰 쌍으로 회전한다. */
    @Transactional
    public TokenPair refresh(String refreshToken) {
        Claims claims = parseRefreshClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        RefreshToken persistedToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> token.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (persistedToken.getExpiresAt().isBefore(clock.instant())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return replaceTokens(getUser(userId));
    }

    /** 유효한 refresh 토큰을 DB에서 삭제해 재발급을 차단한다. */
    @Transactional
    public void logout(String refreshToken) {
        try {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenRepository.findByToken(refreshToken)
                    .filter(token -> token.getUserId().equals(userId))
                    .ifPresent(token -> refreshTokenRepository.deleteByUserId(userId));
        } catch (JwtException | IllegalArgumentException ignored) {
            // 이미 만료되거나 위조된 쿠키도 클라이언트에서는 삭제한다. 서버 상태는 변경하지 않는다.
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private TokenPair replaceTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        Instant expiresAt = jwtTokenProvider.parseClaims(refreshToken).getExpiration().toInstant();

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.create(user.getId(), refreshToken, expiresAt));
        return new TokenPair(accessToken, refreshToken);
    }

    private Claims parseRefreshClaims(String refreshToken) {
        try {
            return jwtTokenProvider.parseClaims(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
