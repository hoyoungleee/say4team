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

    @Deprecated
    public Order toEntity(OrderRequestDto dto, Long userId) {
        OrderStatus orderStatus = OrderStatus.PENDING_USER_FAILURE;

        List<OrderItem> orderItems = dto.getOrderItems().stream()
                .map(item -> toOrderItemEntity(item, null))
                .collect(Collectors.toList());

        Order order = Order.builder()
                .totalPrice(null) // 가격 외부 계산
                .orderStatus(orderStatus)
                .address(null) // 주소 외부 주입
                .orderItems(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        return order;
    }

    @Deprecated
    private OrderItem toOrderItemEntity(OrderItemDto dto, Order order) {
        return OrderItem.builder()
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .unitPrice(null) // 단가 외부에서 세팅
                .order(order)
                .build();
    }

    // 조회용: 상품 정보 포함된 DTO 반환
    public OrderResponseDto toDto(Order order, Map<Long, ProductResDto> productMap) {
        List<OrderItemDto> orderItems = order.getOrderItems().stream()
                .map(item -> {
                    ProductResDto product = productMap.get(item.getProductId());
                    return new OrderItemDto(
                            item.getProductId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            product != null ? product.getName() : null,
                            product != null ? product.getMainImagePath() : null,
                            product != null ? product.getCategoryName() : null
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
