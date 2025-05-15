package com.playdata.orderingservice.ordering.dto;

import lombok.*;

import java.math.BigDecimal;

@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class OrderItemDto {
    private Long productId; // 상품 아이디
    private int quantity; // 수량
    private BigDecimal unitPrice; // 상품 가격
    private String productName; // 상품 이름
    private String mainImagePath; // 메인 이미지
    private String categoryName; // 카테고리 이름

}
