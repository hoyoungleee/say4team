package com.playdata.orderingservice.ordering.dto;

import lombok.Data;
import java.util.List;

// 주문 생성 요청 DTO
@Data
public class OrderRequestDto {
    private List<Long> cartItemIds;    // 장바구니 주문용
    private Long directProductId;      // 바로 주문용 상품 ID
    private int quantity;              // 바로 주문 수량
    private String address;            // 배송지
    private String email;              // (선택) 주문 이메일
}
