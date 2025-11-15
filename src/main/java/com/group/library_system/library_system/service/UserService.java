package com.group.library_system.library_system.service;

import com.group.library_system.library_system.repository.User;
import com.group.library_system.library_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

}
