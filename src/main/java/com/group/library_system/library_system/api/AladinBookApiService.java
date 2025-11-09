package com.group.library_system.library_system.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.library_system.library_system.api.dto.AladinResponse;
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

    public AladinResponse searchBook(String isbn) throws JsonProcessingException {
        String responseString = aladinWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("ItemId", isbn)
                        .queryParam("ItemIdType", "ISBN13")
                        .queryParam("Sort", "CustomerRating")
                        .queryParam("Output", "js")
                        .queryParam("start", 1)
                        .queryParam("maxResult", 1)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("알라딘 API 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(String.class) // 일단 String으로 받음
                .block();

        // 2. JS 함수 제거 → 순수 JSON만 남김
        if (responseString.startsWith("aladinjs(")) {
            responseString = responseString.substring("aladinjs(".length(), responseString.length() - 1);
        }

        responseString = responseString.replaceAll("'", "\"");

        // 3. ObjectMapper로 JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        AladinResponse response = objectMapper.readValue(responseString, AladinResponse.class);

        return response;

    }
}