package com.playdata.orderingservice.cart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId; // 상품 ID

    private int quantity; // 수량

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    public void addQuantity(int quantity) {
        this.quantity += quantity; // 수량을 추가하는 메서드
    }
}

