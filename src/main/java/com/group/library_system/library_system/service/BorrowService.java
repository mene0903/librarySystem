package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BorrowRepository borrowRepository;

    @Transactional
    public void saveInfo(String userId, String isbn) throws JsonProcessingException {
        User user = userRepository.findById(userId).get();
        bookService.saveBook(isbn);
        Book book = bookRepository.findByIsbn(isbn).get();

        Borrow newBorrow = Borrow.builder()
                .borrowId(null)
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(3))
                .build();

        borrowRepository.save(newBorrow);
    }
}
