package com.playdata.orderingservice.ordering.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/*
주문 생성 요청 DTO
*/
@Data
public class OrderRequestDto {
    private List<OrderItemDto> orderItems;
    private String address;

    public BigDecimal getTotalPrice() {
        return orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}