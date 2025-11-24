package com.group.library_system.library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long> {
    Optional<BookRecommend> findByUserAndCategoryId(User user,int categoryId);

    Optional<BookRecommend> findTopByUserOrderByCategoryCountDesc(User user);

    boolean existsByUser(User user);
}
