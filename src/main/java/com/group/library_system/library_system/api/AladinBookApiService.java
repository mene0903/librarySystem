package com.group.library_system.library_system.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.ArrayList;
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

    public AladinResponse searchBook(String isbn) throws JsonProcessingException {

        String responseString = lookupClient.get()
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

    public AladinResponse searchBestSeller() throws JsonProcessingException {

        String responseString = itemListClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("QueryType", "BestSeller")
                        .queryParam("SearchTarget", "Book")
                        .queryParam("Start" , 1)
                        .queryParam("MaxResults", 20)
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
    public AladinResponse lookupBooksByIsbn(List<String> isbnList) throws JsonProcessingException {
        if (isbnList.isEmpty()) return new AladinResponse();

        String isbns = String.join(",", isbnList); // ISBN을 콤마로 연결
        String responseString = lookupClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("TTBKey", clientId)
                        .queryParam("ItemIdType", "ISBN")
                        .queryParam("ItemId", isbns)
                        .queryParam("Output", "js")
                        .queryParam("Version", "20131101")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        if (responseString != null && responseString.startsWith("aladinjs(")) {
            responseString = responseString.substring("aladinjs(".length(), responseString.length() - 1);
        }

        Map<String, Object> map = objectMapper.readValue(responseString, new TypeReference<>() {});
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("item");
        if (items == null) items = Collections.emptyList(); // null이면 빈 리스트로

        List<AladinBookItem> itemList = items.stream()
                .map(itemMap -> objectMapper.convertValue(itemMap, AladinBookItem.class))
                .collect(Collectors.toList());

        AladinResponse response = new AladinResponse();
        response.setItem(itemList);
        return response;
    }


}