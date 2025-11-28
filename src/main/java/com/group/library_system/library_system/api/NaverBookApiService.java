package com.group.library_system.library_system.api;

import com.group.library_system.library_system.api.dto.NaverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
public class NaverBookApiService {

    private final WebClient naverWebClient;

    //keyword 매개변수로 작가/책 검색
    public NaverResponse searchBook(String keyword) {
        return naverWebClient.get()
                // 쿼리 파라미터 설정 (keyword를 URLEncoder.encode 할 필요가 WebClient에서는 없습니다.)
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", keyword)
                        .queryParam("display", 30)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("네이버 API 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(NaverResponse.class)
                .block();
    }
}