package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.repository.BookRecommend;
import com.group.library_system.library_system.repository.BookRecommendRepository;
import com.group.library_system.library_system.repository.BookRepository;
import com.group.library_system.library_system.repository.User;
import com.group.library_system.library_system.service.BorrowService;
import com.group.library_system.library_system.service.UserService;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
public class BookRecommendServiceTest {

    @Autowired
    BorrowService borrowService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookRecommendRepository bookRecommendRepository;

    @Autowired
    UserService userService;

    @Autowired
    BookRecommendService bookRecommendService;

    User createTestUser(String id) {
        return new User(null, "Kim", id, "eee", null, "010-0000", null, 0,0,0);
    }

    @Test
    @DisplayName("책 추천 db 저장 성공 (new user)")
    void recommendSuccess() throws JsonProcessingException  {
        //given
        User user = createTestUser("123");
        userService.registerUser(user);
        borrowService.saveBorrow(user.getId(), "9788937460777");

        int categoryId = bookRepository.findByIsbn("9788937460777").get().getCategoryId();
        //when
        borrowService.returnBook(user.getId(), "9788937460777");
        //then
        Optional<BookRecommend> byUserAndCategoryId = bookRecommendRepository.findByUserAndCategoryId(user, categoryId);
        Assertions.assertThat(byUserAndCategoryId).isPresent();
    }

    @Test
    @DisplayName("책 추천 db 저장 성공 (existing user)")
    void recommendExistingUserSuccess() throws JsonProcessingException  {
        //given
        User user = createTestUser("123");
        userService.registerUser(user);
        borrowService.saveBorrow(user.getId(), "9788937460777");
        borrowService.saveBorrow(user.getId(), "9788925554990"); //다른 책

        int categoryId = bookRepository.findByIsbn("9788937460777").get().getCategoryId();
        int categoryId1 = bookRepository.findByIsbn("9788925554990").get().getCategoryId();
        //when
        borrowService.returnBook(user.getId(), "9788937460777");
        borrowService.returnBook(user.getId(), "9788925554990");
        //then
        Assertions.assertThat(categoryId).isEqualTo(categoryId1);
        Assertions.assertThat(bookRecommendRepository.findByUserAndCategoryId(user, categoryId).get().getCategoryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("대출한 책 페이지 평균 저장 성공")
    void pageUpdateSuccess() throws JsonProcessingException {
        //given
        User user = createTestUser("123");
        userService.registerUser(user);
        borrowService.saveBorrow(user.getId(), "9788937460777");
        //when
        borrowService.returnBook(user.getId(), "9788937460777");
        //then
        int borrowCount = user.getBorrowCount();
        int borrowMean = user.getBorrowMean();
        int borrowCountMean = user.getBorrowCountMean();

        Assertions.assertThat(borrowCount).isEqualTo(1);
        System.out.println("borrowMean = " + borrowMean + "borrowCountMean = " + borrowCountMean);
    }

    @Test
    @DisplayName("사용자 선호 책 추천 -> 대출 기록 없을때 (bestSeller 추천)")
    void recommendBookBestSeller() throws JsonProcessingException {
        //given
        User user = createTestUser("123");
        userService.registerUser(user);
        //when
        List<AladinBookItem> aladinBookItems = bookRecommendService.recommendBook(user);
        //then
        Assertions.assertThat(aladinBookItems).isNotEmpty();
        AladinBookItem aladinBookItem = aladinBookItems.get(0);
        System.out.println(aladinBookItem.getTitle());
        System.out.println(aladinBookItem.getAuthor());
        System.out.println(aladinBookItem.getCover());
        System.out.println(aladinBookItem.getBookinfo());
        System.out.println(aladinBookItem.getCategoryId());
    }

    @Test
    @DisplayName("사용자 선호 책 추천 -> 대출 기록 있을 때")
    void recommendBookBorrow() throws JsonProcessingException{
        //given
        User user = createTestUser("123");
        userService.registerUser(user);
        borrowService.saveBorrow(user.getId(), "9788937460777");
        borrowService.returnBook(user.getId(),"9788937460777" );
        //when
        List<AladinBookItem> aladinBookItems = bookRecommendService.recommendBook(user);
        //then
        Assertions.assertThat(aladinBookItems).isNotEmpty();

        for(int i=0; i<aladinBookItems.size(); i++) {
            AladinBookItem aladinBookItem = aladinBookItems.get(i);
            System.out.println(aladinBookItem.getTitle());
        }

    }
}