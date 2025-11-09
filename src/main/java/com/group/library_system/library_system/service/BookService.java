package com.group.library_system.library_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.NaverBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.dto.AladinResponse;
import com.group.library_system.library_system.api.dto.NaverResponse;
import com.group.library_system.library_system.api.dto.NaverBookItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final NaverBookApiService naverBookApiService;
    private final AladinBookApiService aladinBookApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<NaverBookItem> searchBookForUserSelectionNaver(String keyword) {
        NaverResponse naverResponse = naverBookApiService.searchBook(keyword);

        return naverResponse != null && naverResponse.getItems() != null
                ? naverResponse.getItems()
                : Collections.emptyList();
    }

    public List<AladinBookItem> getAladinDetailsByIsbn(String isbn) throws JsonProcessingException {
        AladinResponse aladinResponse = aladinBookApiService.searchBook(isbn);

        return aladinResponse != null && aladinResponse.getItem() != null
                ? aladinResponse.getItem ()
                : Collections.emptyList();


    }
}
