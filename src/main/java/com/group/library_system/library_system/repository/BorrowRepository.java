package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    boolean existsByBookIsbn(String isbn);

    Optional<Borrow> findByBookIsbn(String isbn);

    Optional<Borrow> findByUser(User user);

    List<Borrow> findAllByUser(User user);
}