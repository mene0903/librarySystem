package com.group.library_system.library_system.controller;

import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.repository.BookRecommendRepository;
import com.group.library_system.library_system.repository.User;
import com.group.library_system.library_system.service.BookRecommendService;
import com.group.library_system.library_system.service.UserService;
import com.group.library_system.library_system.service.BorrowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // 👈 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap; // 👈 추가
import java.util.List;
import java.util.Map;     // 👈 추가

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookRecommendService bookRecommendService;
    private final UserService userService;
    private final AladinBookApiService aladinBookApiService;
    private final BookRecommendRepository bookRecommendRepository;
    private final BorrowService borrowService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // ... (기존 홈 로직 유지) ...
        User loginUser = (User) session.getAttribute("loginUser");

        List<AladinBookItem> bookList = new ArrayList<>();
        String sectionTitle = "";

        try {
            if (loginUser == null) {
                bookList = aladinBookApiService.searchBestSeller().getItem();
                sectionTitle = "지금 서점에서 가장 인기 있는 책 🔥";
            }
            else {
                boolean hasData = bookRecommendRepository.existsByUser(loginUser);
                if (!hasData) {
                    bookList = aladinBookApiService.searchBestSeller().getItem();
                    sectionTitle = loginUser.getName() + "님, 인기도서부터 시작해보세요! 📚";
                }
                else {
                    bookList = bookRecommendService.recommendBook(loginUser);
                    sectionTitle = loginUser.getName() + "님을 위한 취향 저격 도서 🎯";
                }
            }
        } catch (Exception e) {
            System.out.println("메인 페이지 에러: " + e.getMessage());
            bookList = Collections.emptyList();
            sectionTitle = "도서 목록을 불러올 수 없습니다.";
        }

        model.addAttribute("recommendList", bookList);
        model.addAttribute("sectionTitle", sectionTitle);

        return "index";
    }

    // ... (로그인, 로그아웃, 회원가입 등 기존 메서드 유지) ...
    @PostMapping("/login")
    public String login(@RequestParam("username") String id,
                        @RequestParam("password") String password,
                        HttpSession session) {
        try {
            User user = userService.login(id, password);
            session.setAttribute("loginUser", user);
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(User user) {
        try {
            userService.registerUser(user);
            return "redirect:/?signupSuccess=true";
        } catch (Exception e) {
            return "redirect:/signup?error";
        }
    }

    @GetMapping("/api/book/detail")
    @ResponseBody
    public AladinBookItem getBookDetail(@RequestParam("isbn") String isbn) {
        try {
            List<AladinBookItem> items = aladinBookApiService.searchBook(isbn).getItem();
            if (items != null && !items.isEmpty()) {
                return items.get(0);
            }
        } catch (Exception e) {
            System.err.println("상세 조회 실패: " + e.getMessage());
        }
        return null;
    }

    // ==========================================
    // 6. 대출 처리 (AJAX용 JSON 반환으로 수정)
    // ==========================================
    @PostMapping("/loan")
    @ResponseBody // 👈 페이지 이동(redirect) 대신 데이터(JSON)만 반환
    public ResponseEntity<Map<String, Object>> borrowBook(@RequestParam("isbn") String isbn,
                                                          HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        // 1. 로그인 체크
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요한 서비스입니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String cleanIsbn = isbn.trim();
            if (cleanIsbn.isEmpty()) {
                throw new IllegalArgumentException("ISBN 값이 비어있습니다.");
            }

            // 2. 대출 서비스 호출
            borrowService.saveBorrow(loginUser.getId(), cleanIsbn);

            // 3. 성공 응답 생성
            response.put("success", true);
            response.put("message", "대출이 완료되었습니다! 📚");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 4. 실패 응답 생성
            System.out.println("대출 실패: " + e.getMessage());
            e.printStackTrace(); // 서버 콘솔 확인용

            response.put("success", false);
            // 에러 메시지를 클라이언트로 보냄 ("이미 대출된 책입니다" 등)
            response.put("message", "대출 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}