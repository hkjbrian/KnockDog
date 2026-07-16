package com.knockdog.domain.home.dto.response;

import com.knockdog.domain.user.entity.Role;

/** 역할에 따라 달라지는 홈 화면의 더미 데이터 계약. */
public sealed interface HomeResponse permits OwnerHomeResponse, DirectorHomeResponse {

    Role role();
}
