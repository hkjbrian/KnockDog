package com.knockdog.domain.home.service;

import com.knockdog.domain.home.dto.response.DirectorHomeResponse;
import com.knockdog.domain.home.dto.response.HomeResponse;
import com.knockdog.domain.home.dto.response.OwnerHomeResponse;
import com.knockdog.domain.user.entity.Role;
import java.util.List;
import org.springframework.stereotype.Service;

/** 역할별 홈 화면에 표시할 데이터를 제공한다. 현재 데이터는 과제 범위의 더미 값이다. */
@Service
public class HomeService {

    /** 역할에 맞는 홈 화면 데이터를 반환한다. */
    public HomeResponse getHome(Role role) {
        return switch (role) {
            case OWNER -> new OwnerHomeResponse(
                    Role.OWNER,
                    List.of("초코"),
                    List.of("멍멍 유치원", "함께걷개 유치원"));
            case DIRECTOR -> new DirectorHomeResponse(
                    Role.DIRECTOR,
                    "멍멍 유치원",
                    List.of("초코", "보리", "두부"),
                    3);
        };
    }
}
