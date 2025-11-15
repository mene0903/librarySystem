package com.group.library_system.library_system.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.service.BookService;
import com.group.library_system.library_system.service.BorrowService;
import com.group.library_system.library_system.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class BorrowServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    BorrowRepository borrowRepository;
    @Autowired
    BorrowService borrowService;

    User createTestUser(String id) {
        return new User(null, "Kim", id, "eee", null, "010-0000", null, 1,1,1);
    }

    @Test
    @DisplayName("borrow 성공")
    void borrowSuccess() throws JsonProcessingException {
        //given
        User user = createTestUser("123");
        //when
        userService.registerUser(user);
        borrowService.saveBorrow(user.getId(), "9788937460777");
        //then
        Assertions.assertThat(borrowRepository.existsByBookIsbn("9788937460777")).isTrue();
    }

    @Test
    @DisplayName("borrow 실패")
    void borrowFail() throws JsonProcessingException {
        User user1 = createTestUser("123");
        User user2 = createTestUser("321");

        userService.registerUser(user1);
        userService.registerUser(user2);

        String isbn = "9788937460777";

        borrowService.saveBorrow(user1.getId(), isbn);

        try {
            borrowService.saveBorrow(user2.getId(), "9788937460777"); // 두 번째 저장 시도
            fail("예외가 발생해야 합니다."); // 예외 안 나오면 테스트 실패
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("이미 대출이 되어있는 책입니다.");
        }
    }

    @Test
    @DisplayName("책 반납 성공")
    void returnBookSuccess() throws JsonProcessingException {
        User user1 = createTestUser("123");
        userService.registerUser(user1);

        String isbn = "9788937460777";
        borrowService.saveBorrow(user1.getId(), isbn);

        borrowService.returnBook(user1.getId(), isbn);

        Assertions.assertThat(borrowRepository.existsByBookIsbn(isbn)).isFalse();
    }

    @Test
    @DisplayName("책 연장")
    void bookRenewSuccess() throws JsonProcessingException {
        User user1 = createTestUser("123");
        userService.registerUser(user1);

        String isbn = "9788937460777";
        borrowService.saveBorrow(user1.getId(), isbn);

        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();

        System.out.println(borrow.getReturnDate());

        LocalDate localDate = borrow.getReturnDate().plusDays(5);

        borrowService.returnDateRenew(user1.getId(), isbn);

        Borrow borrow2 = borrowRepository.findByBookIsbn(isbn).get();

        Assertions.assertThat(borrow2.getReturnDate()).isEqualTo(localDate);
        System.out.println(borrow2.getReturnDateRenew());
    }

    @Test
    @DisplayName("책 연장 실패")
    void bookRenewFail() throws JsonProcessingException {
        User user1 = createTestUser("123");
        userService.registerUser(user1);

        String isbn = "9788937460777";
        borrowService.saveBorrow(user1.getId(), isbn);

        borrowService.returnDateRenew(user1.getId(), isbn);

        try {
            borrowService.returnDateRenew(user1.getId(), isbn); // 두 번째 저장 시도
            fail("예외가 발생해야 합니다."); // 예외 안 나오면 테스트 실패
        } catch (IllegalStateException e) {
            // 메시지까지 검증 가능
            assertThat(e.getMessage()).isEqualTo("이미 연장된 책입니다.");
        }
    }

}
