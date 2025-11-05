package com.group.library_system.library_system.api;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AladinBookApiService {

    private final WebClient aladinWebClient;

    @Value("${aladin.client.id}")
    private String clientId;

    public String searchBook(String keyword) {
        return aladinWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("Query", keyword)
                        .queryParam("QueryType", "title")
                        .queryParam("Start" , 1)
                        .queryParam("maxresult", 10)
                        .queryParam("sort", "CustomerRating")
                        .queryParam("Output", "js")
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("알라딘 API 호출 실패: " + clientResponse.statusCode());

                })
                .bodyToMono(String.class)
                .block();
    }
}
