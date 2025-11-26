package com.group.library_system.library_system.service;

import com.group.library_system.library_system.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookRecommendRepository bookRecommendRepository;
    private final BorrowRepository borrowRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       BookRepository bookRepository,
                       BorrowRepository borrowRepository,
                       BookRecommendRepository bookRecommendRepository) {
        this.userRepository = userRepository;
        this.bookRecommendRepository = bookRecommendRepository;
        this.borrowRepository = borrowRepository;
    }


    @Transactional
    public void registerUser(User user) {   //회원 가입

        if(userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("아이디가 중복되었습니다.");
        }

        userRepository.save(user);  //예외가 발생하지 않으면 회원 정보 저장
    }

    public User login(String id, String password) {
        Optional<User> ID = userRepository.findById(id);

        if (ID.isEmpty()) {
            // 아이디가 존재하지 않으면 예외 발생
            throw new IllegalArgumentException("아이디가 존재하지 않습니다.");
        }

        User user = ID.get();

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    public void deleteUser(User user) {

        if(borrowRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("책을 모두 반납한 후 탈퇴할 수 있습니다.");
        }

        bookRecommendRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }

    @Transactional
    public void updateUser(User user) {
// 1. DB에서 원본 데이터 가져오기 (변하지 않는 ID 기준)
        // (HTML form에서 readonly로 넘어온 id를 사용)
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 2. [핵심] 값이 있을 때만 수정 (Null 또는 빈 문자열 체크)

        // (1) 비밀번호: 입력값이 있을 때만 변경 (입력 안 하면 기존 비번 유지)
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }

        // (2) 이름: 입력값이 있을 때만 변경
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            existingUser.setName(user.getName());
        }

        // (3) 전화번호: 입력값이 있을 때만 변경
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }

        // 3. 변경 감지(Dirty Checking)에 의해 트랜잭션 종료 시 자동 UPDATE 쿼리 실행
        // (혹은 명시적으로 userRepository.save(existingUser); 해도 됩니다)
    }
}


/*
borrowId -> borrow에서 받아옴
bookRecommend -> userId 지우기
 */