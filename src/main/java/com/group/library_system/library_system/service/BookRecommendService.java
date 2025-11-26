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

        // 1. 사용자 데이터 기반 필터링 기준 설정
        int userPage = user.getBorrowCountMean();  // 사용자 평균 페이지 수
        BookRecommend bookRecommend = bookRecommendRepository.findTopByUserOrderByCategoryCountDesc(user).get(); // 가장 많이 읽은 카테고리
        userPage*=3;

        int minPage, maxPage;
        if (userPage <= 400) {
            minPage = 0;
            maxPage = 400;
        } else if (userPage <= 800) {
            minPage = 400;
            maxPage = 800;
        } else {
            minPage = 800;
            maxPage = 3000;
        }

        List<AladinBookItem> result = new ArrayList<>();
        Set<String> seenBooks = new HashSet<>();   // 중복 제거용 (ex: 초판본, 특별판 등)

        int start = 1;
        int maxResult = 50;  // 한 번에 가져올 후보 책 수
        int maxStart = 200;  // 검색 제한 (너무 많이 검색하면 느려지므로)

        // 목표 개수(20개)를 채울 때까지 반복
        while (result.size() < 20 && start <= maxStart) {

            // 2. 알라딘 API 호출 (해당 카테고리의 인기도서 50권 가져오기)
            AladinResponse aladinResponse = aladinBookApiService.searchRatingBook(bookRecommend.getCategoryId(), start, maxResult);
            List<AladinBookItem> items = aladinResponse.getItem();

            // 가져온 책이 없으면 반복 중단
            if (items == null || items.isEmpty()) break;

            // 3. [핵심] Nici API 병렬 호출 (속도 향상 구간 ⚡️)
            // 50권의 책에 대해 동시에 '페이지 수 확인' 요청을 보냅니다.
            List<AladinBookItem> validItems = items.parallelStream()
                    .map(item -> {
                        try {
                            // (1) Nici API 호출 (오래 걸리는 작업)
                            NiciResponse niciResponse = niciBookApiService.searchPage(item.getIsbn13());

                            // (2) 응답 결과 검증
                            if (niciResponse != null && niciResponse.getDocs() != null && !niciResponse.getDocs().isEmpty()) {
                                String pageStr = niciResponse.getDocs().get(0).getPage();
                                // 숫자만 추출 ("300쪽" -> "300")
                                String pageStrInt = pageStr.replaceAll("[^0-9]", "");

                                if (!pageStrInt.isEmpty()) {
                                    int page = Integer.parseInt(pageStrInt);

                                    // (3) 페이지 조건 만족 여부 확인
                                    if (page >= minPage && page <= maxPage) {
                                        return item; // 조건에 맞는 책만 반환
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // API 호출 중 에러 발생 시(타임아웃 등) 해당 책은 건너뜀
                        }
                        return null; // 조건에 안 맞거나 에러 나면 null 반환
                    })
                    .filter(Objects::nonNull) // null(조건 탈락한 책) 제거
                    .collect(Collectors.toList()); // 리스트로 수집

            // 4. [순차 처리] 중복 제거 및 최종 결과 담기
            for (AladinBookItem validItem : validItems) {
                // 20개가 다 찼으면 즉시 종료
                if (result.size() >= 20) break;

                // 중복 판별을 위한 고유 키 생성 (제목_작가)
                String rawTitle = validItem.getTitle();
                String cleanTitle = rawTitle.replaceAll("[\\(-].*", "")    // 괄호, 하이픈 뒤 제거
                        .replaceAll("\\s\\d+$", "")    // 뒤쪽 숫자 제거
                        .trim();

                String cleanAuthor = validItem.getAuthor().split(",")[0]
                        .split("\\(")[0]
                        .trim();

                String uniqueKey = cleanTitle + "_" + cleanAuthor;

                // 이미 추천 목록에 없는 책만 추가
                if (!seenBooks.contains(uniqueKey)) {
                    result.add(validItem);
                    seenBooks.add(uniqueKey);
                }
            }

            // 다음 50권 검색을 위해 start 인덱스 증가
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

/*
key
startRowNumApi
endRowNemApi
drCode  분류번호(11:문학, 6:인문과학, 5:사회과학, 4:자연과학)
 */