package com.group.library_system.library_system.api;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AladinApiConfig {

    private final String clientId;

    public AladinApiConfig (
            @Value("${aladin.client.id}") String clientId
    ) {
        this.clientId = clientId;
    }

    @Bean
    public WebClient aladinWebClient() {
        return WebClient.builder()
                .baseUrl("http://www.aladin.co.kr/ttb/api/ItemSearch.aspx")
                .build();
    }
}
