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
import java.util.List;

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
        AladinBookItem aladinResult = resultJson.getItem().get(0);
        System.out.println("Title: " + aladinResult.getTitle());
        System.out.println("Author: " + aladinResult.getAuthor());
        System.out.println("ISBN13: " + aladinResult.getIsbn13());
        System.out.println("CategoryId: " + aladinResult.getCategoryId());
        System.out.println("Publisher: " + aladinResult.getPublisher());
        System.out.println("CustomerReviewRank: " + aladinResult.getCustomerReviewRank());
        System.out.println("ItemPage: " + aladinResult.getBookinfo().getItemPage());
        System.out.println("image: " + aladinResult.getCover());
    }

    @Test
    @DisplayName("APi 호출 시 관련 장르 응답 성공")
    void categorySuccess() throws JsonProcessingException {
        //given
        AladinResponse resultJson;
        //when
        resultJson = aladinBookApiService.searchRatingBook("6734");
        //then
        Assertions.assertThat(resultJson).isNotNull();
        Assertions.assertThat(resultJson.getItem()).isNotEmpty();

        System.out.println(resultJson);
        AladinBookItem aladinResult = resultJson.getItem().get(0);
        System.out.println("Title: " + aladinResult.getTitle());
        System.out.println("Author: " + aladinResult.getAuthor());
        System.out.println("ISBN13: " + aladinResult.getIsbn13());
        System.out.println("CategoryId: " + aladinResult.getCategoryId());
        System.out.println("Publisher: " + aladinResult.getPublisher());
        System.out.println("CustomerReviewRank: " + aladinResult.getCustomerReviewRank());
        System.out.println("image: " + aladinResult.getCover());

    }
}
