package com.group.library_system.library_system.repository;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookrecommend")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_recommend_index")
    private Long bookRecommendIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // DB 컬럼 이름 지정
    private User user;

    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "category_count")
    private int categoryCount;


}
