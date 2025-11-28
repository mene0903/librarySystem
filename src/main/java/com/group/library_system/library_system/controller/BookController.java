package com.group.library_system.library_system.controller;

import com.group.library_system.library_system.api.AladinBookApiService;
import com.group.library_system.library_system.api.dto.AladinBookItem;
import com.group.library_system.library_system.api.NaverBookApiService; // 👈 import 추가
import com.group.library_system.library_system.api.dto.NaverResponse;
import com.group.library_system.library_system.api.dto.NaverBookItem;   // 👈 DTO import 확인 필요
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


    //index 페이지
    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "0") int categoryId, // 카테고리 번호 받기
                       @RequestParam(required = false) String mode, // 추천 모드 확인 (?mode=recommend)
                       Model model,
                       HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");
        List<AladinBookItem> bookList = new ArrayList<>();
        String sectionTitle = "";

        try {
            // 1. [사용자 맞춤 추천 모드] (버튼 클릭 시)
            if ("recommend".equals(mode) && loginUser != null) {
                boolean hasData = bookRecommendRepository.existsByUser(loginUser);

                if (hasData) {
                    // 데이터가 충분하면 추천 알고리즘 실행
                    bookList = bookRecommendService.recommendBook(loginUser);
                    sectionTitle = loginUser.getName() + "님을 위한 취향 저격 도서 🎯";
                } else {
                    // 데이터가 없으면 종합 베스트셀러 보여주면서 안내
                    bookList = aladinBookApiService.searchBestSeller(0).getItem();
                    sectionTitle = loginUser.getName() + "님, 아직 데이터가 부족해요! 인기도서부터 읽어보세요 📚";
                }
                model.addAttribute("currentMode", "recommend"); // 버튼 활성화용
            }
            // 2. [카테고리별 베스트셀러] (종합 포함)
            else {
                // Service에 categoryId를 전달 (0이면 종합, 1이면 소설 등)
                // ★ 주의: AladinBookApiService에 파라미터 받는 searchBestSeller(int)가 있어야 함
                var response = aladinBookApiService.searchBestSeller(categoryId);

                if (response != null && response.getItem() != null) {
                    bookList = response.getItem();
                }

                // 제목 설정 (헬퍼 메서드 사용)
                sectionTitle = getCategoryName(categoryId);
                model.addAttribute("currentCategory", categoryId); // 버튼 활성화용
            }

        } catch (Exception e) {
            System.out.println("메인 페이지 에러: " + e.getMessage());
            e.printStackTrace();
            bookList = Collections.emptyList();
            sectionTitle = "도서 목록을 불러올 수 없습니다.";
        }

        if (bookList != null) {
            for (AladinBookItem item : bookList) {
                String originalCover = item.getCover();
                if (originalCover != null) {
                    // 알라딘 이미지 URL 규칙: coversum(작은거) -> cover500(큰거)
                    String highRes = originalCover.replace("coversum", "cover500");
                    item.setCover(highRes);
                }
            }
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

    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    //회원가입
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    //회원가입
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

    //웹에서 받아온 isbn으로 알라딘 책 세부정보 검색
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

    //대출 처리
    @PostMapping("/loan")
    @ResponseBody
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


    //검색 기능 (추가됨)
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

    //개인 페이지
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

                //Borrow 엔티티에 있는 날짜 정보를 DTO에 넣기
                if (borrow.getReturnDate() != null) {
                    item.setReturnDate(borrow.getReturnDate().format(formatter));
                }
                if (borrow.getBorrowDate() != null) { // Service에선 borrowDate로 저장됨
                    item.setLoanDate(borrow.getBorrowDate().format(formatter));
                }

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

    //회원 정보 수정
    @PostMapping("/user/update")
    public String updateUser(User formUser, HttpSession session) { // formUser: 화면에서 입력한 값(비번, 이름 등)

        // 1. 현재 로그인된 사용자 정보 가져오기
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/"; // 로그인이 풀렸으면 홈으로
        }

        try {
            //콘솔 로그로 값 확인
            System.out.println("=== 회원 정보 수정 요청 ===");
            System.out.println("대상 ID (세션): " + loginUser.getId());
            System.out.println("변경할 이름: " + formUser.getName());
            System.out.println("변경할 비번: " + formUser.getPassword());

            // 2. [핵심] 폼에서 넘어온 ID 대신, 세션의 ID를 formUser에 강제 주입
            // (HTML input name이 틀려도, 이걸로 해결됨)
            formUser.setId(loginUser.getId());

            // 3. 업데이트 서비스 호출
            userService.updateUser(formUser);

            // 4. 세션 정보 최신화
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
            // 에러 내용을 콘솔에 출력
            e.printStackTrace();
            System.out.println("수정 실패 원인: " + e.getMessage());

            // 에러 메시지를 화면으로 전달
            return "redirect:/mypage?error=" + URLEncoder.encode("수정 실패", StandardCharsets.UTF_8);
        }
    }

    //회원 탈퇴
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
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/mypage?error=" + errorMsg;

        } catch (NumberFormatException e) {
            // ID가 숫자가 아닐 때
            return "redirect:/mypage?error=" + URLEncoder.encode("잘못된 회원 ID 형식입니다.", StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/mypage?error=" + URLEncoder.encode("탈퇴 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
        }
    }

    // 도서 반납
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

    //도서 연장
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
    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 0: return "지금 서점에서 가장 인기 있는 책 🔥";
            case 1: return "소설/시/희곡 베스트셀러 📖";
            case 170: return "경제경영 베스트셀러 💰";
            case 987: return "과학 베스트셀러 🧪";
            case 656: return "인문학 베스트셀러 🏛️";
            case 336: return "자기계발 베스트셀러 ✨";
            case 55889: return "에세이 베스트셀러 ✍️";
            case 351: return "IT/컴퓨터 베스트셀러 💻";
            case 74: return "역사 베스트셀러 ⏳";
            default: return "도서 베스트셀러 📚";
        }
    }
}