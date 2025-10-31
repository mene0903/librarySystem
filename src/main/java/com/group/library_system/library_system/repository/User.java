package com.group.library_system.library_system.repository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate; // DB의 DATE 타입에 대응

@Entity
@Table(name = "user") // MariaDB에 생성한 테이블 이름과 일치 (대소문자 유의)
@Getter // Lombok: 모든 필드에 대한 Getter 자동 생성
@Setter // Lombok: 모든 필드에 대한 Setter 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성 (JPA 필수)
@AllArgsConstructor // Lombok: 모든 필드를 인수로 받는 생성자 자동 생성
public class User {

    @Id // 이 필드가 PK임을 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // DB 컬럼명: user_id (PK)
    private Long userId;

    @Column(name = "name" , nullable = false, length = 50) // NOT NULL, 길이 50자
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
    private LocalDate membershipDate; // DB의 DATE 타입에 맞춰 LocalDate 사용


    @PrePersist
    protected void onCreate() {
        // 만약 필드에 값이 명시적으로 설정되지 않았다면 (null 이라면)
        if (this.membershipDate == null) {
            // 현재 날짜를 자동으로 할당
            this.membershipDate = LocalDate.now();
        }
    }
    // 기존의 생성자 및 Getter/Setter는 Lombok 어노테이션이 대체합니다.
}