package com.knockdog.domain.home.dto.response;

import com.knockdog.domain.user.entity.Role;
import java.util.List;

/** 견주용 홈 화면의 더미 데이터. */
public record OwnerHomeResponse(
        Role role,
        List<String> myDogs,
        List<String> nearbyKindergartens) implements HomeResponse {
}
