package com.group.library_system.library_system.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AladinBookApiService {

    private final WebClient lookupClient;
    private final WebClient itemListClient;
    private final String clientId;

    public AladinBookApiService(
            @Qualifier("lookupClient") WebClient lookupClient,
            @Qualifier("ItemList") WebClient itemListClient,
            @Value("${aladin.client.id}") String clientId
    ) {
        this.lookupClient = lookupClient;
        this.itemListClient = itemListClient;
        this.clientId = clientId;
    }

    //isbn으로 책 검색 -> 상세정보 불러올 수 있음
    public AladinResponse searchBook(String isbn) throws JsonProcessingException {
        String responseString = lookupClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("ItemId", isbn)
                        .queryParam("ItemIdType", "ISBN13")
                        .queryParam("Cover", "Big")
                        .queryParam("Output", "js")
                        .queryParam("Version", "20131101")
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("알라딘 API 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        // 2. JS 함수 제거
        if (responseString != null && responseString.startsWith("aladinjs(")) {
            responseString = responseString.substring("aladinjs(".length(), responseString.length() - 1);
        }

        // 4. Map으로 변환
        Map<String, Object> map = objectMapper.readValue(responseString, new TypeReference<>() {});
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("item");

        // 5. DTO로 변환 (오류 수정: AladinBookItem 사용)
        List<AladinBookItem> itemList = items.stream()
                .map(itemMap -> objectMapper.convertValue(itemMap, AladinBookItem.class))
                .collect(Collectors.toList());

        AladinResponse response = new AladinResponse();
        response.setItem(itemList);
        return response;


    }

    //알라딘 장르 별 베스트셀러 출력 -> 전체 베스트셀러 -> categoryID = 0
    public AladinResponse searchRatingBook(int categoryId, int start, int maxResult) throws JsonProcessingException {

        String responseString = itemListClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("QueryType", "BestSeller")
                        .queryParam("CategoryId", categoryId)
                        .queryParam("Sort", "CustomerRating")
                        .queryParam("Start", start)
                        .queryParam("MaxResults", maxResult)
                        .queryParam("SearchTarget", "Book")
                        .queryParam("Output", "js")
                        .queryParam("Version", "20131101")
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("알라딘 API 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        // 2. JS 함수 제거
        if (responseString != null && responseString.startsWith("aladinjs(")) {
            responseString = responseString.substring("aladinjs(".length(), responseString.length() - 1);
        }

        // 4. Map으로 변환
        Map<String, Object> map = objectMapper.readValue(responseString, new TypeReference<>() {});
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("item");

        // 5. DTO로 변환 (오류 수정: AladinBookItem 사용)
        List<AladinBookItem> itemList = items.stream()
                .map(itemMap -> objectMapper.convertValue(itemMap, AladinBookItem.class))
                .collect(Collectors.toList());

        AladinResponse response = new AladinResponse();
        response.setItem(itemList);
        return response;
    }

    //알라딘 장르 별 베스트셀러 출력 -> 전체 베스트셀러 -> categoryID = 0
    public AladinResponse searchBestSeller(int categoryId) throws JsonProcessingException {

        String responseString = itemListClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("QueryType", "BestSeller")
                        .queryParam("SearchTarget", "Book")
                        .queryParam("CategoryId", categoryId)
                        .queryParam("Start" , 1)
                        .queryParam("MaxResults", 50)
                        .queryParam("Output" , "js")
                        .queryParam("Version", "20131101")
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("알라딘 API 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        // 2. JS 함수 제거
        if (responseString != null && responseString.startsWith("aladinjs(")) {
            responseString = responseString.substring("aladinjs(".length(), responseString.length() - 1);
        }

        // 4. Map으로 변환
        Map<String, Object> map = objectMapper.readValue(responseString, new TypeReference<>() {});
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("item");

        // 5. DTO로 변환 (오류 수정: AladinBookItem 사용)
        List<AladinBookItem> itemList = items.stream()
                .map(itemMap -> objectMapper.convertValue(itemMap, AladinBookItem.class))
                .collect(Collectors.toList());

        AladinResponse response = new AladinResponse();
        response.setItem(itemList);
        return response;
    }
}