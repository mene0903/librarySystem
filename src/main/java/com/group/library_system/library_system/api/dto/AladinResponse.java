package com.group.library_system.library_system.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinResponse {
    private String version;
    private String title;
    private String link;
    private String pubDate;
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    private String query;

    @JsonProperty("searchCategoryId")
    private Integer searchCategoryId;

    @JsonProperty("searchCategoryName")
    private String searchCategoryName;

    private List<AladinBookItem> item;
}
