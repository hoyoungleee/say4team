package com.playdata.orderingservice.ordering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId; // 주문아이디

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice; // 총결제금액

    // 전달받은 상태로 설정
    @Setter
    @Enumerated(EnumType.STRING) // 주문 상태가 enum 타입이라 DB에 문자열로 넣어줌
    @Builder.Default
    @Column(name = "order_status")
    private OrderStatus orderStatus = OrderStatus.ORDERED; // 주문 상태 (기본값으로 주문완료)

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt; // 주문일자

    @Column(name = "email")
    private String email; // 유저 아이디

    @Column(name = "address", nullable = false)
    private String address; // 주소

    // orphanRemoval = true : 부모 객체에서 자식 객체가 제거되면 자동으로 데이터베이스에서도 삭제됨.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems; // 1개의 주문 -> 여러 개의 주문 항목

}
