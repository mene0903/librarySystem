package com.group.library_system.library_system.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import jakarta.persistence.Temporal;
import org.springframework.ui.Model;
import com.group.library_system.library_system.api.dto.NaverBookItem;
import com.group.library_system.library_system.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller // 👈 이 클래스가 웹 요청을 처리하는 컨트롤러임을 명시
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService; // BookService가 주입됩니다.

    @GetMapping("/book/search") // 👈 사용자가 접속하는 URL 경로 설정
    public String searchBooksPage(@RequestParam(required = false) String keyword, Model model) {

        if (keyword != null && !keyword.isEmpty()) {
            // Service를 통해 API 검색 결과를 가져옵니다.
            List<NaverBookItem> searchResults = bookService.searchBookForUserSelectionNaver(keyword);

            // 결과를 'books'라는 이름으로 Model에 담아 HTML 템플릿에 전달합니다.
            model.addAttribute("books", searchResults);
            model.addAttribute("keyword", keyword);
        }

        // templates 폴더의 'book_search.html' 파일을 찾아서 반환합니다.
        return "book_search";
    }

    @PostMapping("/book/select")
    public String viewBookDetail(@RequestParam String isbn, Model model) throws JsonProcessingException {
        System.out.println("선택한 ISBN: " + isbn); // ✅ 여기에 값이 찍히는지 확인

        List<AladinBookItem> details = bookService.getAladinDetailsByIsbn(isbn);

        if(details.isEmpty()) return "redirect:/book/search?error=detailNotFound";

        model.addAttribute("bookDetail", details.get(0));

        return "book_detail";
    }

}
