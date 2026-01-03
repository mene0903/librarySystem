package com.group.library_system.library_system.service;

import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.NiciBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
import com.group.library_system.library_system.api.dto.NiciResponse;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
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

    /*
    사용자가 반납한 책의 장르 저장
    처음 빌린 장르이기에 categoryCount=1로 고정
     */
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

    /*
    사용자가 반납한 책의 장르 업데이트
    이미 저장이 된 장르일 경우 +1
     */
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
    private static final Map<String, Integer> PAGE_CACHE = new ConcurrentHashMap<>();

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(30);

    /*
    매개변수로 받은 user의 책 추천을 위한 장르 return
    user db에 저장한 사용자 일 평균 읽은 페이지와 장르를 통합해 책 추천
    알라딘 API에서 장르를 받아와 국립중앙도서관 API에서 책 페이지를 불러와 일치하는 페이지는 저장, 일치하지 않으면 저장X
    책 추천 최대 20개, 만약 책 200개를 비교했는데 20개가 안되면 저장된 값만 return
     */
    @Transactional
    public List<AladinBookItem> recommendBook(User user) {

        int userPage = user.getBorrowCountMean() * 3;
        BookRecommend bookRecommend = bookRecommendRepository.findTopByUserOrderByCategoryCountDesc(user).orElseThrow();

        int minPage = (userPage <= 400) ? 0 : (userPage <= 800 ? 400 : 800);
        int maxPage = (userPage <= 400) ? 400 : (userPage <= 800 ? 800 : 10000);

        //알라딘에서 책 후보군 200개(50개씩 4페이지)를 동시에 가져옵니다.
        List<CompletableFuture<List<AladinBookItem>>> aladinFutures = new ArrayList<>();
        int categoryId = bookRecommend.getCategoryId();

        for (int i = 0; i < 4; i++) { // 1, 51, 101, 151 (총 200권 조회)
            int start = 1 + (i * 50);
            aladinFutures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    AladinResponse response = aladinBookApiService.searchRatingBook(categoryId, start, 50);
                    return response.getItem() != null ? response.getItem() : new ArrayList<>();
                } catch (Exception e) {
                    return new ArrayList<>(); // 에러 나면 빈 리스트 반환
                }
            }, EXECUTOR));
        }

        // 알라딘 응답이 모두 올 때까지 기다린 후 하나의 리스트로 합칩니다.
        List<AladinBookItem> allCandidates = aladinFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        //200개의 책에 대해 Nici 페이지 체크를 병렬로 수행합니다.
        List<CompletableFuture<AladinBookItem>> filterFutures = allCandidates.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    if (isPageValid(item.getIsbn13(), minPage, maxPage)) {
                        return item;
                    }
                    return null;
                }, EXECUTOR))
                .collect(Collectors.toList());

        //결과 수집 및 중복 제거 (선착순 20개)
        List<AladinBookItem> result = new ArrayList<>();
        Set<String> seenBooks = new HashSet<>();

        for (CompletableFuture<AladinBookItem> future : filterFutures) {
            if (result.size() >= 20) break; // 20개 차면 즉시 종료

            try {
                AladinBookItem item = future.join(); // 먼저 완료된 순서가 아니라 리스트 순서대로 확인
                if (item != null) {
                    String uniqueKey = generateUniqueKey(item);
                    if (!seenBooks.contains(uniqueKey)) {
                        result.add(item);
                        seenBooks.add(uniqueKey);
                    }
                }
            } catch (Exception e) {
                // 개별 작업 실패는 전체 로직에 영향 주지 않음
            }
        }
        return result;
    }

    private boolean isPageValid(String isbn, int min, int max) {
        try {
            // 1. 캐시 확인
            if (PAGE_CACHE.containsKey(isbn)) {
                int page = PAGE_CACHE.get(isbn);
                return page >= min && page <= max;
            }

            // 2. 캐시 없으면 API 호출
            NiciResponse response = niciBookApiService.searchPage(isbn);
            if (response != null && response.getDocs() != null && !response.getDocs().isEmpty()) {
                String pageStr = response.getDocs().get(0).getPage().replaceAll("[^0-9]", "");
                if (!pageStr.isEmpty()) {
                    int page = Integer.parseInt(pageStr);
                    PAGE_CACHE.put(isbn, page); // 캐시에 저장
                    return page >= min && page <= max;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private String generateUniqueKey(AladinBookItem item) {
        String cleanTitle = item.getTitle().replaceAll("[\\(-].*", "").replaceAll("\\s\\d+$", "").trim();
        String cleanAuthor = item.getAuthor().split(",")[0].split("\\(")[0].trim();
        return cleanTitle + "_" + cleanAuthor;
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