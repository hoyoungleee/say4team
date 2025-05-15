package com.playdata.orderingservice.ordering.dto;

import lombok.Data;
import java.util.List;


// 주문 생성 요청 DTO

@Data
public class OrderRequestDto {
    private List<OrderItemDto> orderItems;
}
