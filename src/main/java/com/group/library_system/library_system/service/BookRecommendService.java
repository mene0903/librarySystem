package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.NiciBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
import com.group.library_system.library_system.api.dto.NiciBookItem;
import com.group.library_system.library_system.api.dto.NiciResponse;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookRecommendService {


    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final BookRecommendRepository bookRecommendRepository;
    private final AladinBookApiService aladinBookApiService;
    private final NiciBookApiService niciBookApiService;


    @Transactional
    public void recommendSave(String userId, String isbn) {
        User user = userRepository.findById(userId).get();
        Book book = bookRepository.findByIsbn(isbn).get();
        int genreId = book.getCategoryId();

        Optional<BookRecommend> optional = bookRecommendRepository.findByUserAndCategoryId(user, genreId);

        if (optional.isPresent()) {
            BookRecommend bookRecommend = optional.get();
            bookRecommend.setCategoryCount(bookRecommend.getCategoryCount() + 1);
        } else {
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

        borrowMean += (int) (pageCount / (int) daysBetween);
        borrowCountMean = borrowMean / borrowCount;

        user.setBorrowMean(borrowMean);
        user.setBorrowCountMean(borrowCountMean);
    }

    @Transactional
    public List<AladinBookItem> recommendBook(User user) throws JsonProcessingException {

        int userPage = user.getBorrowCountMean();  //사용자 평균 페이지
        BookRecommend bookRecommend = bookRecommendRepository.findTopByUserOrderByCategoryCountDesc(user).get();
        int minPage, maxPage;

        if (userPage <= 300) {
            minPage = 0;
            maxPage = 300;
        } else if (userPage <= 600) {
            minPage = 300;
            maxPage = 600;
        } else {
            minPage = 600;
            maxPage = 3000;
        }

        List<AladinBookItem> result = new ArrayList<>();
        Set<String> seenBooks = new HashSet<>();   //책 중복 제거 ex) 초반본, 특별판

        int start = 1;
        int maxResult = 50;
        int maxStart = 200;

        while (result.size() < 20 && start <= maxStart) {
            AladinResponse aladinResponse = aladinBookApiService.searchRatingBook(bookRecommend.getCategoryId(), start, maxResult);
            List<AladinBookItem> items = aladinResponse.getItem();
            for(int i=0; i < items.size(); i++) {

                if(result.size() == 20) break;

                AladinBookItem aladinBookItem = items.get(i);
                String rawTitle = aladinBookItem.getTitle();
                String rawAuthor = aladinBookItem.getAuthor();

                String cleanTitle = rawTitle
                        .replaceAll("[\\(-].*", "")    // 1. '(' 또는 '-'가 나오면 그 뒤의 모든 것을 삭제
                        .replaceAll("\\s\\d+$", "")     // 2. 제목 맨 끝에 '공백+숫자'가 남으면 삭제 (ex: "바벨 2" -> "바벨")
                        .trim();                        // 3. 앞뒤 공백 제거

                String cleanAuthor = rawAuthor.split(",")[0].split("\\(")[0].trim();
                String uniqueKey = cleanTitle + "_" + cleanAuthor;

                if (seenBooks.contains(uniqueKey)) {
                    continue;
                }

                NiciResponse niciResponse = niciBookApiService.searchPage(aladinBookItem.getIsbn13());

                if (niciResponse == null || niciResponse.getDocs() == null || niciResponse.getDocs().isEmpty()) {
                    continue;
                }

                String pageStr = niciResponse.getDocs().get(0).getPage();
                String pageStrInt = pageStr.replaceAll("[^0-9]", "");
                if(pageStrInt.isEmpty()) continue; // 숫자가 없으면 건너뛰기

                int page= Integer.parseInt(pageStrInt);
                if (page >= minPage && page <= maxPage)  {
                    result.add(aladinBookItem);
                    seenBooks.add(uniqueKey);
                }

            }
            start += 50;
        }
        return result;
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