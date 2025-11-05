package com.group.library_system.library_system.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
public class AladinBookApiServiceTest {

    @Autowired
    private AladinBookApiService aladinBookApiService;

    @Test
    @DisplayName("API 호출시 json 응답 성공")
    void aladinSuccess() {
        //given
        String keyword = "개미";
        String resultJson;
        //when
        resultJson = aladinBookApiService.searchBook(keyword);
        Assertions.assertThat(resultJson).isNotNull();
        Assertions.assertThat(resultJson).contains("개미");

        System.out.println(resultJson);
    }
}
