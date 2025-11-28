package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BorrowRepository borrowRepository;
    private final BookRecommendService bookRecommendService;

    /*
    id와 isbn을 통해 user, 책 값을 받아와 저장
    user 값을 받은 후 user db에 있는 빌린 횟수 +1
    BookService에서 isbn 확인 후 대출된 책이 아닐 경우 저장
    저장 후 book db에 책 저장
     */
    @Transactional
    public void saveBorrow(String userId, String isbn) throws JsonProcessingException {
        User user = userRepository.findById(userId).get();
        user.setBorrowCount(user.getBorrowCount() + 1);
        bookService.saveBook(isbn);
        Book book = bookRepository.findByIsbn(isbn).get();

        Borrow newBorrow = Borrow.builder()
                .borrowId(null)
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(5))
                .returnDateRenew(0)
                .build();

        borrowRepository.save(newBorrow);
    }

    /*
    책 반납
    borrow db에 있는 책 정보 제거
    book db에 있는 책 정보 제거
     */
    @Transactional
    public void returnBook(String userId, String isbn) throws JsonProcessingException {

        User user = userRepository.findById(userId).get();
        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();
        Book book = bookRepository.findByIsbn(isbn).get();

        bookRecommendService.recommendSave(userId, isbn);
        bookRecommendService.updateMean(user, isbn);

        borrowRepository.delete(borrow);
        bookRepository.delete(book);
    }

    /*
    책 반납 날짜 연장 (1번 가능)
    borrow db에 returnDateRenew가 1일 경우 예외처리
    연장 기록이 없을 경우 반납 날짜 3일 추가
     */
    @Transactional
    public void returnDateRenew(String userId, String isbn) throws JsonProcessingException {
        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();
        Book book = bookRepository.findById(borrow.getBook().getBookId()).get();

        if(borrow.getReturnDateRenew() == 1) throw new IllegalStateException("이미 연장된 책입니다.");

        borrow.setReturnDate(borrow.getReturnDate().plusDays(3));
        borrow.setReturnDateRenew(borrow.getReturnDateRenew() + 1);

        book.setReturnDate(borrow.getReturnDate());
    }

    //매개변수로 받은 user의 대출 상황을 list로 return
    @Transactional
    public List<Borrow> listUserBookRecommend(User user) {
        return borrowRepository.findAllByUser(user);
    }

    //매개변수로 받은 user의 대출 상황을 list로 return
    @Transactional
    public List<Borrow> findUserBorrowList(User user) {
        return borrowRepository.findAllByUser(user);
    }
}