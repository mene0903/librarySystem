package com.group.library_system.library_system.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.repository.Book;
import com.group.library_system.library_system.repository.BookRepository;
import com.group.library_system.library_system.service.BookService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    private Book createToBook(String isbn) {
        return new Book(null, "1984", "조지 오웰", 123, 1234, 1.56F, isbn, "2022-20-01");
    }

    @Test
    @DisplayName("Book 저장 성공")
    void bookRegisterSuccess() throws JsonProcessingException {
        //given
        String isbn1 = "9788937460777";
        //when
        bookService.saveBook(isbn1);
        //then
        Assertions.assertThat(bookRepository.existsByIsbn(isbn1)).isTrue();
    }

    @Test
    @DisplayName("Book 저장 실패")
    void bookRegisterFail() throws JsonProcessingException {
        //given
        String isbn1 = "9788937460777";
        String isbn2 = "9788937460777";
        //when
        bookService.saveBook(isbn1);
        try {
            bookService.saveBook(isbn2); // 두 번째 저장 시도
            fail("예외가 발생해야 합니다."); // 예외 안 나오면 테스트 실패
        } catch (IllegalArgumentException e) {
            // 메시지까지 검증 가능
            assertThat(e.getMessage()).isEqualTo("이미 대출이 되어있는 책입니다.");
        }

        //then


    }
}
