package com.group.library_system.library_system.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long> {
    Optional<BookRecommend> findByUserAndCategoryId(User user,int categoryId);

    Optional<BookRecommend> findTopByUserOrderByCategoryCountDesc(User user);

    boolean existsByUser(User user);

    @Transactional
    void deleteAllByUser(User user);
}
