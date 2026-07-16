package com.knockdog.domain.home.dto.response;

import com.knockdog.domain.user.entity.Role;
import java.util.List;

/** 원장용 홈 화면의 더미 데이터. */
public record DirectorHomeResponse(
        Role role,
        String kindergartenName,
        List<String> enrolledDogs,
        int todayAttendance) implements HomeResponse {
}
