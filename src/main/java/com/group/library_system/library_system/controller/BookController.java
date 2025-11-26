package com.group.library_system.library_system.controller;

import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.NaverBookApiService; // 👈 import 추가
import com.group.library_system.library_system.api.dto.NaverResponse;
import com.group.library_system.library_system.api.dto.NaverBookItem;   // 👈 DTO import 확인 필요
import com.group.library_system.library_system.api.dto.NaverResponse;   // 👈 DTO import 확인 필요
import com.group.library_system.library_system.repository.Book;
import com.group.library_system.library_system.repository.BookRecommendRepository;
import com.group.library_system.library_system.repository.Borrow;
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
import java.net.URLEncoder; // 인코딩을 위해 필요
import java.nio.charset.StandardCharsets;

import java.time.format.DateTimeFormatter;
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
    private final NaverBookApiService naverBookApiService;
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
            return "redirect:/";
        } catch (Exception e) {
            // [수정] 로그인 실패 시 에러 파라미터를 붙여서 리다이렉트
            System.out.println("로그인 실패: " + e.getMessage());
            return "redirect:/?loginError=true";
        }
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
            System.err.println("회원가입 실패(중복 등): " + e.getMessage());

            // [수정] 그냥 ?error가 아니라 ?error=duplicate 라고 명시해서 보냄
            return "redirect:/signup?error=duplicate";
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


    // ==========================================
    // 🔍 검색 기능 (추가됨)
    // ==========================================
    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword,
                         Model model,
                         HttpSession session) {

        List<AladinBookItem> viewList = new ArrayList<>();
        String sectionTitle = "'" + keyword + "' 검색 결과 🔍";

        try {
            // 1. 네이버 API 호출
            NaverResponse response = naverBookApiService.searchBook(keyword);

            // 2. 결과가 있다면 변환 작업 수행
            if (response != null && response.getItems() != null) {
                for (NaverBookItem naverItem : response.getItems()) {
                    // 3. 네이버 검색 결과(NaverBookItem)를 기존 뷰 포맷(AladinBookItem)으로 변환
                    AladinBookItem item = new AladinBookItem();

                    // (1) 제목, 저자 매핑
                    item.setTitle(naverItem.getTitle());
                    item.setAuthor(naverItem.getAuthor());

                    // (2) 이미지 -> cover 매핑
                    item.setCover(naverItem.getImage());

                    // (3) isbn -> isbn13 매핑
                    item.setIsbn13(naverItem.getIsbn());

                    // (4) 상세 설명 -> description 매핑
                    item.setDescription(naverItem.getDescription());

                    viewList.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("검색 에러: " + e.getMessage());
            e.printStackTrace();
            sectionTitle = "검색 중 오류가 발생했습니다.";
        }

        // 4. 모델에 담아서 index.html 재사용
        model.addAttribute("recommendList", viewList);
        model.addAttribute("sectionTitle", sectionTitle);

        return "index";
    }

    @GetMapping("/mypage")
    public String myPage(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/";
        }

        // 1. [수정] Service에서 Borrow 리스트를 받아옵니다.
        List<Borrow> borrowList = borrowService.findUserBorrowList(loginUser);

        List<AladinBookItem> borrowedList = new ArrayList<>();

        // 날짜를 "2025-10-14" 형식으로 바꾸기 위한 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (borrowList != null) {
            for (Borrow borrow : borrowList) {
                // Borrow 안에서 Book 꺼내기
                Book book = borrow.getBook();

                AladinBookItem item = new AladinBookItem();

                // 2. 책 정보 매핑
                item.setTitle(book.getTitle());
                item.setAuthor(book.getAuthor());
                item.setCover(book.getBookImage());
                item.setIsbn13(book.getIsbn()); // DB의 ISBN을 DTO의 isbn13에 매핑

                // 3. [핵심] Borrow 엔티티에 있는 날짜 정보를 DTO에 넣기
                if (borrow.getReturnDate() != null) {
                    item.setReturnDate(borrow.getReturnDate().format(formatter));
                }
                if (borrow.getBorrowDate() != null) { // Service에선 borrowDate로 저장됨
                    item.setLoanDate(borrow.getBorrowDate().format(formatter));
                }

                // (선택사항) 설명 필드에도 넣고 싶다면 유지
                item.setDescription(
                        "Due Date: " + item.getReturnDate() + "<br>" +
                                "Loan Date: " + item.getLoanDate()
                );

                borrowedList.add(item);
            }
        }

        model.addAttribute("borrowedList", borrowedList);
        return "mypage";
    }

    // ==========================================
    // [추가] 2. 회원 정보 수정
    // ==========================================
    @PostMapping("/user/update")
    public String updateUser(User formUser, HttpSession session) { // formUser: 화면에서 입력한 값(비번, 이름 등)

        // 1. 현재 로그인된 사용자 정보 가져오기 (가장 확실한 ID 출처)
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/"; // 로그인이 풀렸으면 홈으로
        }

        try {
            // [디버깅] 콘솔 로그로 값 확인 (실행 후 인텔리제이 콘솔 확인해보세요)
            System.out.println("=== 회원 정보 수정 요청 ===");
            System.out.println("대상 ID (세션): " + loginUser.getId());
            System.out.println("변경할 이름: " + formUser.getName());
            System.out.println("변경할 비번: " + formUser.getPassword());

            // 2. [핵심] 폼에서 넘어온 ID 대신, 세션의 ID를 formUser에 강제로 주입
            // (HTML input name이 틀려도, 이걸로 해결됨)
            formUser.setId(loginUser.getId());

            // 3. 업데이트 서비스 호출
            userService.updateUser(formUser);

            // 4. 세션 정보도 최신화 (화면에 반영되도록)
            // (비밀번호는 세션 객체에 굳이 업데이트 안 해도 되지만, 이름/번호는 해야 함)
            if (formUser.getName() != null && !formUser.getName().trim().isEmpty()) {
                loginUser.setName(formUser.getName());
            }
            if (formUser.getPhoneNumber() != null && !formUser.getPhoneNumber().trim().isEmpty()) {
                loginUser.setPhoneNumber(formUser.getPhoneNumber());
            }

            // 세션 다시 저장
            session.setAttribute("loginUser", loginUser);

            return "redirect:/mypage?updateSuccess=true";

        } catch (Exception e) {
            // 에러 내용을 콘솔에 자세히 출력
            e.printStackTrace();
            System.out.println("수정 실패 원인: " + e.getMessage());

            // 에러 메시지를 화면으로 전달
            return "redirect:/mypage?error=" + URLEncoder.encode("수정 실패", StandardCharsets.UTF_8);
        }
    }    // ==========================================
    // [추가] 3. 회원 탈퇴
    // ==========================================
    @PostMapping("/user/delete")
    public String deleteUser(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        // 1. 로그인 안 된 상태면 메인으로 쫓아냄
        if (loginUser == null) {
            return "redirect:/";
        }

        try {
            // 3. 서비스 호출 (삭제 시도)
            userService.deleteUser(loginUser);

            // 4. 성공 시 세션 비우고 메인으로 (성공 메시지 전달)
            session.invalidate();
            return "redirect:/?message=" + URLEncoder.encode("회원 탈퇴가 완료되었습니다.", StandardCharsets.UTF_8);

        } catch (IllegalStateException e) {
            // [중요] 서비스에서 "책 반납하세요" 라고 던진 에러를 잡는 곳
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/mypage?error=" + errorMsg;

        } catch (NumberFormatException e) {
            // ID가 숫자가 아닐 때
            return "redirect:/mypage?error=" + URLEncoder.encode("잘못된 회원 ID 형식입니다.", StandardCharsets.UTF_8);

        } catch (Exception e) {
            // 그 외 알 수 없는 에러
            e.printStackTrace();
            return "redirect:/mypage?error=" + URLEncoder.encode("탈퇴 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
        }
    }    // ==========================================
    // [추가] 4. 도서 반납 (AJAX)
    // ==========================================
    @PostMapping("/return")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> returnBook(@RequestParam("isbn") String isbn,
                                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // BorrowService에서 반납 처리 (DB 업데이트)
            borrowService.returnBook(loginUser.getId(), isbn);

            response.put("success", true);
            response.put("message", "반납이 완료되었습니다. 📗");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "반납 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // BookController.java에 추가

    // ==========================================
// [추가] 도서 연장 (AJAX)
// ==========================================
    @PostMapping("/renew")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> renewBook(@RequestParam("isbn") String isbn,
                                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loginUser = (User) session.getAttribute("loginUser");

        // 1. 로그인 체크
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 2. 연장 서비스 호출
            // *주의: BorrowService의 returnDateRenew는 userId를 받지 않으므로,
            //       userId 확인 로직이 필요하거나 Service 함수를 수정해야 할 수 있습니다.
            //       (제공된 Service 코드 기준으로 일단 호출)

            // 현재 Service 코드가 String userId를 받으므로, loginUser의 ID를 넘깁니다.
            borrowService.returnDateRenew(loginUser.getId(), isbn);

            response.put("success", true);
            response.put("message", "대출 기간이 5일 연장되었습니다! 📅");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // "이미 연장된 책입니다." 에러 처리
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            // 기타 에러 처리
            response.put("success", false);
            response.put("message", "연장 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}