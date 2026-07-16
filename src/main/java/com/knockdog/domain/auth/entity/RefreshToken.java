package com.knockdog.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DB에 저장하는 refresh 토큰 (ADR-0003·0004). 로그아웃 시 삭제하고, 재발급 시 존재 여부로 유효성을 확인한다.
 *
 * <p>rich domain: public setter 없이 정적 팩토리 {@link #create}로만 생성한다.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private RefreshToken(Long userId, String token, Instant expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken create(Long userId, String token, Instant expiresAt) {
        return new RefreshToken(userId, token, expiresAt);
    }
}
