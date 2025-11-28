package com.group.library_system.library_system.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinBookItem {

    @JsonAlias({"isbn", "isbn13", "ISBN", "ISBN13"})
    private String isbn13;               //isbn
    @JsonAlias({"Title", "title"})
    private String title;                //제목
    @JsonAlias({"Author", "author"})
    private String author;               //작가

    @JsonProperty(value = "categoryId")
    @JsonAlias({"categoryId", "searchCategoryId"})
    private int categoryId;             // 장르 ID
    private float customerReviewRank;   //사용자 평점
    private String publisher;           //출판사
    @JsonAlias({"Cover", "cover"})
    private String cover;               //책 표지 (url)
    private String pubDate;             //출판일
    private String description;         //설명

    @JsonAlias("subInfo")
    private BookInfo bookinfo;          //책 페이지

    private String returnDate;          // 반납 예정일
    private String loanDate;            // 대출일

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookInfo  {
        private int itemPage;
    }


}