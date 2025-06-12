package com.playdata.orderingservice.ordering.mapper;

import com.playdata.orderingservice.ordering.dto.*;
import com.playdata.orderingservice.ordering.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    // 조회용 메서드
    public OrderResponseDto toDto(Order order, Map<Long, ProductResDto> productMap) {
        List<OrderItemDto> orderItems = order.getOrderItems().stream()
                .map(item -> {
                    ProductResDto product = productMap.get(item.getProductId());
                    return new OrderItemDto(
                            item.getOrderItemId(),
                            item.getProductId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            product != null ? product.getName() : null,
                            product != null ? product.getMainImagePath() : null,
                            product != null ? product.getCategoryName() : null,
                            item.getOrderStatus().name()
                    );
                })
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus().name())
                .orderedAt(order.getOrderedAt())
                .address(order.getAddress())
                .email(order.getEmail())
                .orderItems(orderItems)
                .build();
    }

}

