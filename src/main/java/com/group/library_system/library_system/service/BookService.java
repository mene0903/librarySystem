package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.NaverBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
import com.group.library_system.library_system.api.dto.NaverResponse;
import com.group.library_system.library_system.api.dto.NaverBookItem;
import com.group.library_system.library_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final NaverBookApiService naverBookApiService;
    private final AladinBookApiService aladinBookApiService;
    private final BookRepository bookRepository;

    //키워드를 통한 값 return
    public List<NaverBookItem> searchBookForUserSelectionNaver(String keyword) {
        NaverResponse naverResponse = naverBookApiService.searchBook(keyword);

        return naverResponse != null && naverResponse.getItems() != null
                ? naverResponse.getItems()
                : Collections.emptyList();
    }

    //isbn을 통해 알라딘 API에서 책의 상세정보 return
    public List<AladinBookItem> getAladinDetailsByIsbn(String isbn) throws JsonProcessingException {
        AladinResponse aladinResponse = aladinBookApiService.searchBook(isbn);

        return aladinResponse != null && aladinResponse.getItem() != null
                ? aladinResponse.getItem ()
                : Collections.emptyList();
    }

    /*
    isbn으로 책의 정보를 가져와 book에 저장
    만약 book db에 동일 isbn이 있을 경우 예외처리
     */
    @Transactional
    public void saveBook(String isbn) throws JsonProcessingException {
        List<AladinBookItem> details = getAladinDetailsByIsbn(isbn);

        if (details.isEmpty() || details.get(0).getBookinfo() == null) {
            throw new IllegalArgumentException("도서 상세 정보를 찾을 수 없습니다.");
        }

        AladinBookItem item = details.get(0);

        if(bookRepository.existsByIsbn(item.getIsbn13())) {
            throw new IllegalArgumentException("이미 대출이 되어있는 책입니다.");
        }

        Book newBook = Book.builder()
                .bookId(null)
                .title(item.getTitle())
                .author(item.getAuthor())
                .categoryId(item.getCategoryId())
                .pageCount(item.getBookinfo().getItemPage())
                .customerReviewRank(item.getCustomerReviewRank())
                .isbn(item.getIsbn13())
                .publishedYear(item.getPubDate())
                .bookImage(item.getCover())
                .returnDate(LocalDate.now().plusDays(5))
                .build();

        bookRepository.save(newBook);
    }

}
