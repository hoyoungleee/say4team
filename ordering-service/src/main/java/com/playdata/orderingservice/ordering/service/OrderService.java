package com.playdata.orderingservice.ordering.service;

import com.playdata.orderingservice.ordering.dto.OrderRequestDto;
import com.playdata.orderingservice.ordering.dto.OrderResponseDto;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import com.playdata.orderingservice.ordering.mapper.OrderMapper;
import com.playdata.orderingservice.ordering.repository.OrderRepository;
import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository; // 주문 데이터베이스 연동
    private final OrderMapper orderMapper; // DTO와 Entity 변환을 위한 Mapper

    // 주문을 생성하는 메서드
    // orderRequestDto 주문 요청 DTO
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        // DTO를 Entity로 변환 (DB에 저장하기 위해)
        Order order = orderMapper.toEntity(orderRequestDto);

        // 주문 날짜와 주문 상태를 생성자에서 설정하도록 처리
        order = Order.builder()
                .totalPrice(order.getTotalPrice())
                .orderStatus(OrderStatus.PENDING_USER_FAILURE)
                .orderedAt(LocalDateTime.now())
                .userId(order.getUserId())
                .address(order.getAddress())
                .orderItems(order.getOrderItems())
                .build();

        // 주문을 DB에 저장
        Order savedOrder = orderRepository.save(order);
        // 저장된 주문을 DTO로 변환하여 반환
        return orderMapper.toDto(savedOrder); // 생성된 주문에 대한 응답 DTO
    }

    // 주문 조회 메서드
    // orderId 주문 ID
    public OrderResponseDto getOrder(Long orderId) {
        // 주문 ID로 주문 조회
        Optional<Order> order = orderRepository.findById(orderId);
        // 주문이 없으면 예외 발생
        if (order.isEmpty()) {
            throw new ServiceUnavailableException("주문 ID가 존재 하지 않습니다: " + orderId);
        }
        // 조회된 주문을 DTO로 변환하여 반환
        return orderMapper.toDto(order.get()); // 주문 응답 DTO
    }


    // 주문 상태 변경 메서드
    // orderId 주문 ID
    // status 변경할 주문 상태
    public OrderResponseDto updateOrderStatus(Long orderId, String status) {
        // 주문 ID로 주문 조회
        Optional<Order> order = orderRepository.findById(orderId);

        // 주문이 없으면 예외 발생
        if (order.isEmpty()) {
            throw new ServiceUnavailableException("주문을 찾을 수 없음. 주문 ID: " + orderId);
        }

        // 기존 주문 객체를 가져오기
        Order orderUpdate = order.get();

        // status 문자열을 OrderStatus enum으로 변환
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status); // status가 유효한 값인지 체크
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 주문 상태입니다: " + status);
        }

        // 기존 주문 정보를 바탕으로 새로운 Order 객체를 생성 (orderStatus만 변경)
        Order updatedOrder = Order.builder()
                .orderId(orderUpdate.getOrderId()) // 기존 주문 ID
                .totalPrice(orderUpdate.getTotalPrice()) // 기존 총 금액
                .orderStatus(orderStatus) // 새로 받은 상태 값으로 설정
                .orderedAt(orderUpdate.getOrderedAt()) // 기존 주문 일자
                .userId(orderUpdate.getUserId()) // 기존 사용자 ID
                .address(orderUpdate.getAddress()) // 기존 주소
                .orderItems(orderUpdate.getOrderItems()) // 기존 주문 항목
                .build();

        // 상태가 변경된 주문을 DB에 저장
        orderRepository.save(updatedOrder);

        // 변경된 주문에 대한 응답 DTO를 반환
        return orderMapper.toDto(updatedOrder); // 상태가 업데이트된 주문을 DTO로 변환하여 반환
    }

}
