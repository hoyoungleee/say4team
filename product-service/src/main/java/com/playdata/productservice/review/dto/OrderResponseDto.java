package com.playdata.productservice.review.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 주문 응답 데이터를 담는 DTO
// 주문 정보를 클라이언트에게 응답할 때 사용
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId; // 주문 ID
    private BigDecimal totalPrice; // 총 가격
    private String orderStatus; // 주문 상태
    private LocalDateTime orderedAt; // 주문 시각
    private String address; // 배송지 주소
    private List<OrderItemDto> orderItems; // 주문 항목 리스트
    private String email;


}
