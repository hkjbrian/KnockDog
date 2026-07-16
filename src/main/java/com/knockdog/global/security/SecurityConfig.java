package com.knockdog.global.security;

import com.knockdog.global.security.handler.RestAccessDeniedHandler;
import com.knockdog.global.security.handler.RestAuthenticationEntryPoint;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 공통 구성.
 *
 * <p>{@link SecurityFilterChain}을 두 개로 분리한다. 근거는 ADR-0008 참조.
 * <ul>
 *     <li>API 체인({@code /api/**}): STATELESS + Bearer 기반. 향후 #12에서
 *         {@code oauth2ResourceServer().jwt()}가 연결된다.</li>
 *     <li>기본 체인: 브라우저 흐름(세션). 향후 #11에서 {@code oauth2Login()}이 연결된다.</li>
 * </ul>
 *
 * <p>이 커밋 시점에서는 인증 방식 자체는 비어 있고, 체인 골격·CORS·401/403 JSON 응답 등
 * 공통 정책만 배선한다.
 */
@Configuration
public class SecurityConfig {

    /** 개발 환경의 프론트엔드 origin. 배포 origin은 별도 프로파일에서 확장한다. */
    private static final String DEV_FRONTEND_ORIGIN = "http://localhost:3000";

    /**
     * API 체인: {@code /api/**} 요청을 STATELESS + JSON 규격으로 처리한다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {

        http
                .securityMatcher("/api/**")
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // TODO(#12): oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.decoder(...)))
                ;

        return http.build();
    }

    /**
     * 기본 체인: 브라우저용. 현재는 모든 요청을 통과시키고, 이후 #11에서 oauth2Login이 연결된다.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain browserSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // TODO(#11): oauth2Login(o -> o.userInfoEndpoint(...).successHandler(...))
                ;

        return http.build();
    }

    /**
     * CORS 정책. 개발용 프론트엔드 origin을 허용하고, 자격 증명 포함 요청을 허용한다.
     * (배포 환경 origin은 별도 프로파일 구성 시 확장 예정.)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(DEV_FRONTEND_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
