package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    //book db에 isbn 검색으로 책이 존재하는지 return
    boolean existsByIsbn(String isbn);

    //isbn을 통해 book return
    Optional<Book> findByIsbn(String isbn);
}