package com.group.library_system.library_system.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class NaverApiConfig {

    private final String clientId;
    private final String clientSecret;

    public NaverApiConfig(
            @Value("${naver.client.id}") String clientId,
            @Value("${naver.client.secret}") String clientSecret) {

        // 필드 초기화는 생성자에서 수행
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Bean
    public WebClient naverWebClient() {
        return WebClient.builder()
                    .baseUrl("https://openapi.naver.com/v1/search/book") // 기본 API URL
                // 💡 필수 인증 헤더 추가
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();
    }
}
