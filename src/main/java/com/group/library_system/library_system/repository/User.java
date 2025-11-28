package com.group.library_system.library_system.repository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate; // DB의 DATE 타입에 대응

/*
변수 이름, 타입 동일하게 매핑
회원가입 날짜 자동으로 현재날짜 입력
 */

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name" , nullable = false, length = 50)
    private String name;

    @Column(name = "id", nullable = false, unique = true, length = 20)
    private String id;

    @Column(name = "password" , nullable = false, length = 255)
    private String password;

    @Column(name = "birth", nullable = true)
    private LocalDate birth;

    @Column(name = "phone_number", nullable = true, unique = true , length = 11)
    private String phoneNumber;

    @Column(name = "membership_date", nullable = false)
    private LocalDate membershipDate;

    @Column(name = "borrow_count", length = 20)
    private int borrowCount;

    @Column(name = "borrow_mean" , length = 20)
    private int borrowMean;

    @Column(name = "borrow_count_mean")
    private int borrowCountMean;

    @PrePersist
        protected void onCreate() {     //날짜 입력값이 null일 경우 현재 날짜로 자동 입력
        if (this.membershipDate == null) {
            this.membershipDate = LocalDate.now();
        }
    }
}
