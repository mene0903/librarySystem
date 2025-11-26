package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BorrowRepository borrowRepository;
    private final BookRecommendService bookRecommendService;

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

    @Transactional
    public void returnDateRenew(String userId, String isbn) throws JsonProcessingException {
        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();
        Book book = bookRepository.findById(borrow.getBook().getBookId()).get();

        if(borrow.getReturnDateRenew() == 1) throw new IllegalStateException("이미 연장된 책입니다.");

        borrow.setReturnDate(borrow.getReturnDate().plusDays(3));
        borrow.setReturnDateRenew(borrow.getReturnDateRenew() + 1);

        book.setReturnDate(borrow.getReturnDate());
    }

    @Transactional
    public List<Borrow> listUserBookRecommend(User user) {
        return borrowRepository.findAllByUser(user);
    }

    @Transactional
    public List<Borrow> findUserBorrowList(User user) {
        return borrowRepository.findAllByUser(user);
    }
}