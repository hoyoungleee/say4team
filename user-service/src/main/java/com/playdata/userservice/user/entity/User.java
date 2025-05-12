package com.playdata.userservice.user.entity;

import com.playdata.userservice.user.dto.UserResDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Entity에 Setter를 구현하지 않은 이유는 Entity 자체가 DB와 연동하기 위한 객체.
// DB에 삽입되는 데이터, DB에서 조회된 데이터는 그 자체로 사용하고 수정되지 않게끔
// setter를 사용하지 않는 것을 권장.
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = true)
    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default // builder 패턴 사용해서 객체 초기화 시 초기값으로 세팅
    private Role role =  Role.USER;

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    @Column(length = 20,nullable = false, name = "phone")
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Order> orders = new ArrayList<>();
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Review> reviews = new ArrayList<>();
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Qna> qnas = new ArrayList<>();
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Wishlist> wishlists = new ArrayList<>();
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Cart> carts = new ArrayList<>();

    // DTO에 Entity 변환 메서드가 있는 거처럼
    // Entity 에도 응답용 DTO 변환 메서드를 세팅해서 언제든 변환이 자유롭도록 작성.
    public UserResDto fromEntity() {
        return UserResDto.builder()
                .userid(userId)
                .name(name)
                .email(email)
                .role(role)
                .address(address)
                .phone(phone)
                .birthdate(birthDate)
                .build();
    }

}






