package com.group.library_system.library_system.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
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
    void aladinSuccess() throws JsonProcessingException {
        //given
        AladinResponse resultJson;
        //when
        resultJson = aladinBookApiService.searchBook("9788937460777");
        Assertions.assertThat(resultJson).isNotNull();
        Assertions.assertThat(resultJson.getItem()).isNotEmpty();

        System.out.println(resultJson);
        AladinBookItem book = resultJson.getItem().get(0);
        System.out.println("Title: " + book.getTitle());
        System.out.println("Author: " + book.getAuthor());
        System.out.println("ISBN13: " + book.getIsbn13());
        System.out.println("CategoryId: " + book.getCategoryId());
        System.out.println("Publisher: " + book.getPublisher());
        System.out.println("CustomerReviewRank: " + book.getCustomerReviewRank());
        System.out.println("ItemPage: " + book.getBookinfo().getItemPage());
        System.out.println("image: " + book.getCover());
    }
}
