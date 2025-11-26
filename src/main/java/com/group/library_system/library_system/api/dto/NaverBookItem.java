package com.group.library_system.library_system.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverBookItem {
    private String title;
    private String author;
    private String image;
    private String isbn;
    private String description;
}
