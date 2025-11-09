package com.group.library_system.library_system.api;

import com.group.library_system.library_system.api.dto.NaverResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
public class NaverBookApiServiceTest {

    @Autowired
    private NaverBookApiService naverBookApiService;
    private NaverResponse response;

    @Test
    @DisplayName("유효한 키워드로 네이버 API 호출 시 JSON 응답을 성공적으로 받는다")
    void NaverApiSuccess() {
        //given
        String keyword = "개미";
        NaverResponse resultJson;
        //when
        response = naverBookApiService.searchBook(keyword);
        //then
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getItems()).isNotEmpty();

        System.out.println("검색된 첫 번째 책 제목: " + response.getItems().get(0).getTitle());
    }
}