package com.knockdog.domain.user.entity;

import com.knockdog.global.exception.BusinessException;
import com.knockdog.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 카카오 로그인으로 생성되는 최소 사용자 정보 (ADR-0002·0004). */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true)
    private Long kakaoId;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private User(Long kakaoId, String nickname, Instant now) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static User create(Long kakaoId, String nickname, Instant now) {
        return new User(kakaoId, nickname, now);
    }

    /** 역할은 온보딩에서 한 번만 확정한다 (ADR-0002). */
    public void assignRole(Role role, Instant now) {
        if (this.role != null) {
            throw new BusinessException(ErrorCode.ROLE_ALREADY_ASSIGNED);
        }
        this.role = role;
        this.updatedAt = now;
    }
}
