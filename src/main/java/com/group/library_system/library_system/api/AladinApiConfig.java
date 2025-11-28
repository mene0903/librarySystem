package com.group.library_system.library_system.api;


import org.springframework.beans.factory.annotation.Qualifier;
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


    //알라딘 책 상세정보 URL
    @Bean
    @Qualifier("lookupClient")
    public WebClient lookupClient() {
        return WebClient.builder()
                .baseUrl("http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx")
                .build();
    }

    //알라딘 책 리스트 URL ex) 베스트셀러 출력 -> 책 상세정보 X, 기본 정보만 있음
    @Bean
    @Qualifier("ItemList")
    public WebClient ItemList() {
        return WebClient.builder()
                .baseUrl("http://www.aladin.co.kr/ttb/api/ItemList.aspx")
                .build();
    }
}
