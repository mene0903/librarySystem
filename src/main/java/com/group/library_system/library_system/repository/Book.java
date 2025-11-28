package com.group.library_system.library_system.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/*
book db랑 연동
변수 이름, 타입 동일하게 매핑
 */
@Entity(name = "bookSave")
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "author" , length = 255)
    private String author;

    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "page_count")
    private int pageCount;

    @Column(name = "customer_review_rank")
    private float customerReviewRank;

    @Column(name = "isbn", length = 255)
    private String isbn;

    @Column(name = "published_year", length = 255)
    private String publishedYear;

    @Column(name = "book_image", length = 255)
    private String bookImage;

    @Column(name = "return_date")
    private LocalDate returnDate;

}
