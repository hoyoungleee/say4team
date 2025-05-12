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
    public Order toEntity(OrderRequestDto dto) {
        // OrderStatus 변환 시 예외 처리 추가
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(dto.getOrderStatus()); // OrderStatus Enum으로 변환
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 주문 상태입니다: " + dto.getOrderStatus());
        }

        // 나머지 코드
        List<OrderItem> orderItems = dto.getOrderItems().stream()
                .map(item -> toOrderItemEntity(item, null))  // null을 임시로 넣어주고 후에 setOrderItems에서 설정
                .collect(Collectors.toList());

        // DTO의 데이터를 기반으로 Order 엔티티 객체 생성
        Order order = Order.builder()
                .userId(dto.getUserId()) // 사용자 ID 설정
                .totalPrice(dto.getTotalPrice()) // 총 가격 설정
                .orderStatus(orderStatus) // 변환된 OrderStatus 설정
                .address(dto.getAddress()) // 주소 설정
                .orderItems(orderItems) // OrderItem 리스트 설정
                .build();

        // OrderItem 엔티티에 올바른 Order 참조 설정
        orderItems.forEach(item -> item.setOrder(order));  // 각 orderItem의 order를 설정

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
