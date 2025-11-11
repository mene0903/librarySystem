package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    boolean existsByBookIsbn(String isbn);
}
