package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    //매개변수로 받은 user가 존재하는지 return
    boolean existsById(String id); //id 중복 확인

    //id를 이용해 user return
    Optional<User> findById(String id); //일반 id값 확인

}
