package com.playdata.orderingservice.ordering.mapper;

import com.playdata.orderingservice.client.ProductServiceClient;
import com.playdata.orderingservice.ordering.dto.*;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.entity.OrderItem;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final ProductServiceClient productServiceClient;

    public OrderMapper(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public Order toEntity(OrderRequestDto dto, Long userId, List<OrderItem> orderItemsFromCart) {
        // 주문 상태 기본값 설정
        OrderStatus orderStatus = OrderStatus.PENDING_USER_FAILURE;

        // cartItemIds 로부터 주문 항목(orderItemsFromCart)을 이미 조회했다고 가정함
        // 따라서 DTO에서 직접 변환하지 않고 외부에서 orderItems를 받음

        Order order = Order.builder()
                .totalPrice(null)
                .orderStatus(orderStatus)
                .address(null)
                .orderItems(orderItemsFromCart)
                .build();

        orderItemsFromCart.forEach(item -> {
            item.setOrder(order);
            item.setOrderStatus(OrderStatus.ORDERED);
        });
        return order;
    }

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

