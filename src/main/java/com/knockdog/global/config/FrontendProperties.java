package com.knockdog.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** OAuth 성공 후 이동할 프론트엔드의 고정 URL. 사용자 입력으로 변경하지 않는다. */
@ConfigurationProperties(prefix = "app")
public record FrontendProperties(String oauthSuccessUrl) {
}
