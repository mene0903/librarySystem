package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();

        bookRecommendService.recommendSave(userId, isbn);

        borrowRepository.delete(borrow);
        bookRepository.delete(bookRepository.findByIsbn(isbn).get());
    }

    @Transactional
    public void returnDateRenew(String userId, String isbn) throws JsonProcessingException {
        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();

        if(borrow.getReturnDateRenew() == 1) throw new IllegalStateException("이미 연장된 책입니다.");

        borrow.setReturnDate(borrow.getReturnDate().plusDays(5));
        borrow.setReturnDateRenew(borrow.getReturnDateRenew() + 1);
    }
}