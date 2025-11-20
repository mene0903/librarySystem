package com.group.library_system.library_system.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NicicApiConfig {

    private final String clientId;

    public NicicApiConfig(
            @Value("${nici.client.id}") String clientId) {
                this.clientId = clientId;
    }

    @Bean
    public WebClient niciWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.nl.go.kr/seoji/SearchApi.do")
                .build();
    }

}