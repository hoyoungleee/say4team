package com.playdata.orderingservice.ordering.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/*
 주문 생성 요청 DTO
 */
@Data
public class OrderRequestDto {

    private Long userId; // 주문을 하는 사용자 ID
    private BigDecimal totalPrice; // 총 가격
    private String orderStatus; // 주문 상태
    private String address; // 배송지 주소
    private List<OrderItemDto> orderItems; // 주문 항목 리스트

}
