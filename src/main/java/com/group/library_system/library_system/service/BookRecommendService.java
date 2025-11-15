package com.group.library_system.library_system.service;

import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookRecommendService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final BookRecommendRepository bookRecommendRepository;

    @Transactional
    public void recommendSave(String userId, String isbn) {
        User user = userRepository.findById(userId).get();
        Book book = bookRepository.findByIsbn(isbn).get();
        int genreId = book.getCategoryId();

        Optional<BookRecommend> optional = bookRecommendRepository.findByUserAndCategoryId(user,genreId);

        if(optional.isPresent()) {
            BookRecommend bookRecommend = optional.get();
            bookRecommend.setCategoryCount(bookRecommend.getCategoryCount() + 1);
        }
        else {
            BookRecommend bookRecommend = BookRecommend.builder()
                    .bookRecommendIndex(null)
                    .user(user)
                    .categoryId(genreId)
                    .categoryCount(1)
                    .build();
            bookRecommendRepository.save(bookRecommend);
        }
    }

    @Transactional
    public void updateMean(User user, String isbn) {
        Borrow borrow = borrowRepository.findByBookIsbn(isbn).get();
        Book book = bookRepository.findByIsbn(isbn).get();

        int borrowCount = user.getBorrowCount();
        int borrowMean = user.getBorrowMean();
        int borrowCountMean = user.getBorrowCountMean();
        int pageCount = book.getPageCount();

        long daysBetween = ChronoUnit.DAYS.between(borrow.getBorrowDate(), LocalDate.now());
        if (daysBetween <= 0) daysBetween = 1;   //대출한 날 반납일 땐 1일로 처리

        borrowMean += (int)(pageCount / (int)daysBetween);
        borrowCountMean = borrowMean / borrowCount;

        user.setBorrowMean(borrowMean);
        user.setBorrowCountMean(borrowCountMean);

    }
}


/*
borrowCountMean = 0
borrowMean
borrowCount = 0

borrowCount += 1
borrowMean += page / (returnDate - borrowDate)
borrowCountMean = borrowMean/borrowCount
 */