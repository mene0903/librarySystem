package com.group.library_system.library_system.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverBookApiService {

    private final WebClient naverWebClient;

    public String searchBook(String keyword) {
        return naverWebClient.get()
                // 쿼리 파라미터 설정 (keyword를 URLEncoder.encode 할 필요가 WebClient에서는 없습니다.)
                .uri(uriBuilder -> uriBuilder
                        .queryParam("d_titl", keyword)
                        .queryParam("display", 10)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("네이버 API 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(String.class) // 응답 본문을 문자열로 받습니다.
                .block(); // 비동기 Mono를 블로킹하여 동기적으로 결과를 얻습니다.    }
    }
}