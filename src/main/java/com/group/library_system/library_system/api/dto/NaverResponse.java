package com.group.library_system.library_system.api.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class NaverResponse {
    private List<NaverBookItem> items;         //책 정보 list로 저장
}