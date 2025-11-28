package com.group.library_system.library_system.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverBookItem {
    private String title;           //제목
    private String author;          //작가
    private String image;           //책 표지
    private String isbn;            //isbn
    private String description;     //책 설명
}
