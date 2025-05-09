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

    /*
      OrderRequestDto를 Order 엔티티로 변환하는 메서드.
      @param dto 주문 요청 DTO
      @return 변환된 Order 엔티티 객체
     */
    public Order toEntity(OrderRequestDto dto) {
        // DTO의 OrderItemDto 리스트를 엔티티의 OrderItem 리스트로 변환
        List<OrderItem> orderItems = dto.getOrderItems().stream()
                .map(this::toOrderItemEntity) // 각각의 OrderItemDto를 OrderItem 엔티티로 변환
                .collect(Collectors.toList());

        // DTO의 데이터를 기반으로 Order 엔티티 객체 생성
        return Order.builder()
                .userId(dto.getUserId()) // 사용자 ID 설정
                .totalPrice(dto.getTotalPrice()) // 총 가격 설정
                .orderStatus(OrderStatus.valueOf(dto.getOrderStatus())) // OrderStatus 문자열을 Enum으로 변환
                .address(dto.getAddress()) // 주소 설정
                .orderItems(orderItems) // OrderItem 리스트 설정
                .build();
    }

    /*
      Order 엔티티를 OrderResponseDto로 변환하는 메서드.

      @param entity 주문 엔티티
      @return 변환된 응답 DTO
     */
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

    /*
      OrderItemDto를 OrderItem 엔티티로 변환하는 메서드.

      @param dto 주문 항목 DTO
      @return 변환된 OrderItem 엔티티
     */
    private OrderItem toOrderItemEntity(OrderItemDto dto) {
        // DTO 데이터를 기반으로 OrderItem 엔티티 객체 생성
        return OrderItem.builder()
                .productId(dto.getProductId()) // 제품 ID 설정
                .quantity(dto.getQuantity()) // 수량 설정
                .unitPrice(dto.getUnitPrice()) // 단가 설정
                .build();
    }

    /*
      OrderItem 엔티티를 OrderItemDto로 변환하는 메서드.

      @param entity 주문 항목 엔티티
      @return 변환된 주문 항목 DTO
     */
    private OrderItemDto toOrderItemDto(OrderItem entity) {
        // 엔티티 데이터를 기반으로 OrderItemDto 객체 생성
        return OrderItemDto.builder()
                .productId(entity.getProductId()) // 제품 ID 설정
                .quantity(entity.getQuantity()) // 수량 설정
                .unitPrice(entity.getUnitPrice()) // 단가 설정
                .build();
    }
}
