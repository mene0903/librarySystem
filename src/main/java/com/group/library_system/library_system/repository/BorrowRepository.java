package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    //사용자가 빌린 목록 중 매개변수로 받은 책이 있는지 return
    boolean existsByBookIsbn(String isbn);

    //isbn으로 borrow db 검색 후 값 return
    Optional<Borrow> findByBookIsbn(String isbn);

    //매개변수로 받은 사용자가 책을 빌린 상태 return
    Optional<Borrow> findByUser(User user);

    //사용자가 빌린 정보 list return
    List<Borrow> findAllByUser(User user);
}