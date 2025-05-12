package com.playdata.orderingservice.ordering.service;

import com.playdata.orderingservice.client.ProductServiceClient;
import com.playdata.orderingservice.client.UserServiceClient;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDto;
import com.playdata.orderingservice.ordering.dto.OrderRequestDto;
import com.playdata.orderingservice.ordering.dto.OrderResponseDto;
import com.playdata.orderingservice.ordering.dto.UserResDto;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.entity.OrderItem;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import com.playdata.orderingservice.ordering.mapper.OrderMapper;
import com.playdata.orderingservice.ordering.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient; // UserServiceClient 주입
    private final ProductServiceClient productServiceClient;

    // 주문 생성
    public Order createOrder(OrderRequestDto orderRequestDto, TokenUserInfo tokenUserInfo) {
        String userEmail = tokenUserInfo.getEmail();  // email을 가져옴

        if (userEmail == null) {
            throw new RuntimeException("토큰에서 사용자 정보를 가져올 수 없습니다.");
        }

        // 이메일을 통해 사용자 정보 조회
        CommonResDto<UserResDto> userResponse = userServiceClient.findByEmail(userEmail);
        if (userResponse == null || userResponse.getResult() == null) {
            throw new RuntimeException("사용자 정보가 없습니다.");
        }

        // 주문 생성
        Order order = Order.builder()
                .totalPrice(orderRequestDto.getTotalPrice())
                .orderStatus(OrderStatus.PENDING_USER_FAILURE)
                .orderedAt(LocalDateTime.now())
                .email(userEmail)  // 이메일로 수정
                .address(orderRequestDto.getAddress())
                .build();

        List<OrderItem> orderItems = orderRequestDto.getOrderItems().stream()
                .map(dto -> OrderItem.builder()
                        .quantity(dto.getQuantity())
                        .unitPrice(dto.getUnitPrice())
                        .productId(dto.getProductId())
                        .order(order)
                        .build())
                .toList();

        order.setOrderItems(orderItems);

        return orderRepository.save(order);  // 주문 저장
    }

    // 사용자의 전체 주문 조회 (이메일로 조회)
    public List<OrderResponseDto> getOrdersByEmail(String email) {
        List<Order> orders = orderRepository.findAllByEmail(email);  // 이메일로 조회
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    // 주문 단건 조회
    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 ID가 존재하지 않습니다: " + orderId));
        return orderMapper.toDto(order);
    }


    // 주문 상태 업데이트
    public OrderResponseDto updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. 주문 ID: " + orderId));

        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 주문 상태입니다: " + status);
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    // 주문 취소
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 주문이 존재하지 않습니다."));

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }
}
