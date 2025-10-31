package com.group.library_system.library_system.repository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public void registerUser(User user) {

        if(userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("아이디가 중복되었습니다.");
        }

        userRepository.save(user);
    }

}
