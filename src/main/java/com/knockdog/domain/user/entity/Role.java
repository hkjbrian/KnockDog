package com.knockdog.domain.user.entity;

/**
 * 사용자 역할 (ADR-0002). 카카오 가입 직후엔 미정(null)이며, 온보딩으로 확정한다.
 *
 * <ul>
 *   <li>{@code OWNER} — 견주
 *   <li>{@code DIRECTOR} — 원장
 * </ul>
 */
public enum Role {
    OWNER,
    DIRECTOR
}
