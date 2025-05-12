package com.playdata.orderingservice.ordering.mapper;

import com.playdata.orderingservice.ordering.dto.OrderItemDto;
import com.playdata.orderingservice.ordering.dto.OrderRequestDto;
import com.playdata.orderingservice.ordering.dto.OrderResponseDto;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.entity.OrderItem;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/*
  DTO와 Entity 간 변환을 담당하는 클래스.
  엔티티 객체와 DTO 객체 간 변환을 수행하여 서비스 계층에서 사용할 수 있도록 함.
 */
@Component
public class OrderMapper {

    // OrderRequestDto를 Order 엔티티로 변환하는 메서드
    // OrderStatus 문자열을 Enum으로 변환하는 부분에 예외 처리 추가
    public Order toEntity(OrderRequestDto dto, Long userId) {
        // 기본 주문 상태를 직접 설정
        OrderStatus orderStatus = OrderStatus.PENDING_USER_FAILURE;

        // OrderItem 엔티티 리스트 생성
        List<OrderItem> orderItems = dto.getOrderItems().stream()
                .map(item -> toOrderItemEntity(item, null)) // order는 나중에 set
                .collect(Collectors.toList());

        // Order 객체 생성
        Order order = Order.builder()
                .userId(userId)
                .totalPrice(dto.getTotalPrice())
                .orderStatus(orderStatus)
                .address(dto.getAddress())
                .orderItems(orderItems)
                .build();

        // 각 OrderItem에 order 참조 설정
        orderItems.forEach(item -> item.setOrder(order));

        return order;
    }



    // 기존처럼 OrderItemDto를 OrderItem 엔티티로 변환하는 메서드
    private OrderItem toOrderItemEntity(OrderItemDto dto, Order order) {
        return OrderItem.builder()
                .productId(dto.getProductId()) // 제품 ID 설정
                .quantity(dto.getQuantity()) // 수량 설정
                .unitPrice(dto.getUnitPrice()) // 단가 설정
                .order(order) // order 설정
                .build();
    }

    // Order 엔티티를 OrderResponseDto로 변환하는 메서드
    public OrderResponseDto toDto(Order entity) {
        // 엔티티의 OrderItem 리스트를 DTO의 OrderItemDto 리스트로 변환
        List<OrderItemDto> orderItems = entity.getOrderItems().stream()
                .map(this::toOrderItemDto) // 각각의 OrderItem 엔티티를 OrderItemDto로 변환
                .collect(Collectors.toList());

        // 엔티티의 데이터를 기반으로 OrderResponseDto 객체 생성
        return OrderResponseDto.builder()
                .orderId(entity.getOrderId()) // 주문 ID 설정
                .totalPrice(entity.getTotalPrice()) // 총 가격 설정
                .orderStatus(entity.getOrderStatus().name()) // OrderStatus Enum을 문자열로 변환
                .orderedAt(entity.getOrderedAt()) // 주문 시간 설정
                .address(entity.getAddress()) // 주소 설정
                .orderItems(orderItems) // OrderItemDto 리스트 설정
                .build();
    }

    // OrderItem 엔티티를 OrderItemDto로 변환하는 메서드
    private OrderItemDto toOrderItemDto(OrderItem entity) {
        return OrderItemDto.builder()
                .productId(entity.getProductId()) // 제품 ID 설정
                .quantity(entity.getQuantity()) // 수량 설정
                .unitPrice(entity.getUnitPrice()) // 단가 설정
                .build();
    }
}
