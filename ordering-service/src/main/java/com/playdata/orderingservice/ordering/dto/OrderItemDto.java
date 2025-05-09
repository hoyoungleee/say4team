package com.playdata.orderingservice.ordering.dto;

import lombok.*;

import java.math.BigDecimal;

/*
 주문 항목 정보를 담는 DTO
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OrderItemDto {
    private Long productId; // 상품 ID
    private int quantity; // 수량
    private BigDecimal unitPrice; // 단가
}
