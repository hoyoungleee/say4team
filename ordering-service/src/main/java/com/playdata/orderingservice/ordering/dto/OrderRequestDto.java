package com.playdata.orderingservice.ordering.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/*
주문 생성 요청 DTO
*/
@Data
public class OrderRequestDto {
    private List<OrderItemDto> orderItems; // 주문 항목 리스트
    private String address; // 주소
    private Long userId; // 사용자의 ID (토큰을 통해 전달받을 수 있음)

    // 총 가격 계산 메서드 추가
    public BigDecimal getTotalPrice() {
        // 각 OrderItemDto의 가격 * 수량을 더한 값 리턴
        return orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
