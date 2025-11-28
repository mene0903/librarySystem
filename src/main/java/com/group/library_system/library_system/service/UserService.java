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

    /*
    매개변수로 받은 user 정보를 user db에 저장
    id 중복 -> 예외처리
    중복이 아닐 경우 db 저장
     */
    @Transactional
    public void registerUser(User user) {   //회원 가입

        if(userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("아이디가 중복되었습니다.");
        }
        userRepository.save(user);
    }

    /*
    id, pw를 통한 로그인
    id, 비밀번호 값이 존재하지 않거나 틀리면 예외처리
    값이 맞을 경우 user 값을 받아와 return
     */
    public User login(String id, String password) {
        Optional<User> ID = userRepository.findById(id);

        if (ID.isEmpty()) {
            throw new IllegalArgumentException("아이디가 존재하지 않습니다.");
        }

        User user = ID.get();

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    /*
    회원 탈퇴
    빌린 책이 존재할 경우 탈퇴 불가
    FK로 연결되어 있기에 bookRecommend user 삭제 후 userRepository user 삭제
     */
    public void deleteUser(User user) {

        if(borrowRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("책을 모두 반납한 후 탈퇴할 수 있습니다.");
        }

        bookRecommendRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }

    /*
    회원 정보 수정
    UI에서 입력한 값만 변경 -> 전화번호만 입력했으면 전화번호 정보만 변경
     */
    @Transactional
    public void updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        //비밀번호: 입력값이 있을 때만 변경
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }

        //이름: 입력값이 있을 때만 변경
        if (user.getName() != null && !user.getName().trim().isEmpty()) {
            existingUser.setName(user.getName());
        }

        //전화번호: 입력값이 있을 때만 변경
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
    }
}