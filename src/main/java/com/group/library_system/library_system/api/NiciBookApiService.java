package com.group.library_system.library_system.api;

import com.group.library_system.library_system.api.dto.NiciResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NiciBookApiService {

    private final WebClient niciWebClient;
    private final String clientId;

    public NiciBookApiService(WebClient niciWebClient,
                              @Value("${nici.client.id}") String clientId) {
        this.niciWebClient = niciWebClient;
        this.clientId = clientId;
    }

    //책 페이지 값을 위한 isbn 검색
    public NiciResponse searchPage(String isbn) {
        return niciWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("cert_key", clientId )
                        .queryParam("result_style", "json")
                        .queryParam("page_no", 1)
                        .queryParam("page_size", 1)
                        .queryParam("isbn" , isbn)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> {
                    throw new RuntimeException("국립중앙도서관 호출 실패: " + clientResponse.statusCode());
                })
                .bodyToMono(NiciResponse.class)
                .block();
    }
}
