package com.group.library_system.library_system.api.dto;

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

    private String isbn13;     //isbn
    private String title;
    private String author;
    private int categoryId; //genreId
    private float customerReviewRank;
    private String publisher;
    private BookInfo bookinfo;
    private String cover;      // ✅ 수정 완료: 최상위 레벨로 이동
    private String pubDate;
    private String description;  // ✅ 추가


    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookInfo  {
        private int itemPage;
    }
}