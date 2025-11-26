package com.group.library_system.library_system.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinBookItem {

    @JsonAlias({"isbn", "isbn13", "ISBN", "ISBN13"})
    private String isbn13;     //isbn
    @JsonAlias({"Title", "title"})
    private String title;
    @JsonAlias({"Author", "author"})
    private String author;

    @JsonProperty(value = "categoryId")
    @JsonAlias({"categoryId", "searchCategoryId"})
    private int categoryId; //genreId
    private float customerReviewRank;
    private String publisher;
    @JsonAlias({"Cover", "cover"})
    private String cover;      // ✅ 수정 완료: 최상위 레벨로 이동
    private String pubDate;
    private String description;  // ✅ 추가

    @JsonAlias("subInfo")
    private BookInfo bookinfo;

    private String returnDate; // 반납 예정일
    private String loanDate;   // 대출일

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookInfo  {
        private int itemPage;
    }


}