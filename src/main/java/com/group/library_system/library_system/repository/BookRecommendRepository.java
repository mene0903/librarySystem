package com.group.library_system.library_system.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long> {
    //user, categoryID 값으로 BookRecommend 리스트 return
    Optional<BookRecommend> findByUserAndCategoryId(User user,int categoryId);

    //사용자의 카테고리 정렬 후 가장 많이 빌린 카테고리 정보 return
    Optional<BookRecommend> findTopByUserOrderByCategoryCountDesc(User user);

    //user 존재하는지 return
    boolean existsByUser(User user);

    //사용자 삭제
    @Transactional
    void deleteAllByUser(User user);
}
