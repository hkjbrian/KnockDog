package com.knockdog.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서버 시각 취급 정책 (ADR-0007). 현재시각은 항상 이 {@link Clock} 빈에서 얻는다. 운영은 UTC, 테스트는
 * {@code Clock.fixed(...)}로 고정한다.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
