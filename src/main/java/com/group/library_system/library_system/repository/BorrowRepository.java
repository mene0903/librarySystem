package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    boolean existsByBookIsbn(String isbn);

    Optional<Borrow> findByBookIsbn(String isbn);
}
