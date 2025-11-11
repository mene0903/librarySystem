package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsById(String id); //id 중복 확인

    Optional<User> findById(String id); //일반 id값 확인

}
