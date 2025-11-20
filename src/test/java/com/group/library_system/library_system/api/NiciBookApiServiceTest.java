package com.group.library_system.library_system.api;

import com.group.library_system.library_system.api.dto.NiciResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
public class NiciBookApiServiceTest {

    @Autowired
    private NiciBookApiService niciBookApiService;

    @Test
    @DisplayName("API 호출 JSON 응답 성공")
    void niciSuccess() {
        //given
        String isbn = "9788931026245";
        //when
        NiciResponse niciResponse = niciBookApiService.searchPage(isbn);
        //then
        Assertions.assertThat(niciResponse.getDocs().get(0).getPage()).isEqualTo("424");
    }
}
