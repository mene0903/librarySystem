package com.group.library_system.library_system.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiciBookItem {
    @JsonProperty("TITLE")
    private String title;

    @JsonProperty("AUTHOR")
    private String author;

    @JsonProperty("PUBLISHER")
    private String publisher;

    @JsonProperty("PAGE")
    private String page;

    @JsonProperty("BOOK_TB_CNT")
    private String bookTbCnt;

    @JsonProperty("SET_ISBN")
    private String setIsbn;

    @JsonProperty("BOOK_SIZE")
    private String bookSize;

    @JsonProperty("FORM")
    private String form;

    @JsonProperty("EBOOK_YN")
    private String ebookYn;

    @JsonProperty("PUBLISHER_URL")
    private String publisherUrl;
}