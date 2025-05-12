package com.playdata.orderingservice.ordering.service;

import com.playdata.orderingservice.client.ProductServiceClient;
import com.playdata.orderingservice.client.UserServiceClient;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDto;
import com.playdata.orderingservice.ordering.dto.OrderRequestDto;
import com.playdata.orderingservice.ordering.dto.OrderResponseDto;
import com.playdata.orderingservice.ordering.dto.ProductResDto;
import com.playdata.orderingservice.ordering.dto.UserResDto;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.entity.OrderItem;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import com.playdata.orderingservice.ordering.mapper.OrderMapper;
import com.playdata.orderingservice.ordering.repository.OrderRepository;
import jakarta.ws.rs.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository; // 주문 데이터베이스 연동
    private final OrderMapper orderMapper; // DTO와 Entity 변환을 위한 Mapper
    private final UserServiceClient userServiceClient; // 사용자 정보를 받아오는 클라이언트
    private final ProductServiceClient productServiceClient;

    // 주문을 생성하는 메서드
    public Order createOrder(OrderRequestDto orderRequestDto, TokenUserInfo tokenUserInfo) {
        // JWT 토큰에서 유저 정보 가져오기
        UserResDto userResDto = getUserResDto(tokenUserInfo.getEmail());
        Long userId = userResDto.getUserId();

        // 주문 엔티티 생성
        Order order = Order.builder()
                .totalPrice(orderRequestDto.getTotalPrice())
                .orderStatus(OrderStatus.PENDING_USER_FAILURE)  // 초기 상태는 USER_FAILURE
                .orderedAt(LocalDateTime.now())
                .userId(userId)
                .address(orderRequestDto.getAddress())
                .build();

        // 주문 항목들을 순차적으로 처리하여 OrderItem 엔티티 생성
        List<OrderItem> orderItems = orderRequestDto.getOrderItems().stream()
                .map(dto -> {
                    // 상품 정보를 조회하여 가격 가져오기
                    ProductResDto product = getProductById(dto.getProductId()); // 상품 서비스에서 가격을 가져옴
                    return OrderItem.builder()
                            .quantity(dto.getQuantity())
                            .unitPrice(product.getPrice()) // 상품의 가격을 unitPrice로 설정
                            .productId(dto.getProductId())
                            .order(order)
                            .build();
                })
                .toList();

        // 생성된 주문 항목들을 주문에 설정
        order.setOrderItems(orderItems);

        // 주문 저장
        return orderRepository.save(order);
    }

    // 상품 정보를 조회하는 메서드
    public ProductResDto getProductById(Long productId) {
        // 상품 서비스에서 상품 정보를 가져오는 로직 (여기서는 예시로 가정)
        return productServiceClient.getProductById(productId);  // productServiceClient는 외부 API 호출 등을 통해 상품 정보 조회
    }

    // 사용자 정보를 가져오는 메서드
    public UserResDto getUserResDto(String email) {
        // 사용자 서비스 클라이언트를 통해 사용자 정보 조회
        CommonResDto<UserResDto> byEmail = userServiceClient.findByEmail(email);

        // 결과 반환
        return byEmail.getResult();
    }

    // 주문 조회 메서드
    public OrderResponseDto getOrder(Long orderId) {
        // 주문 ID로 주문 조회
        Optional<Order> order = orderRepository.findById(orderId);

        // 주문이 없으면 예외 발생
        if (order.isEmpty()) {
            throw new ServiceUnavailableException("주문 ID가 존재 하지 않습니다: " + orderId);
        }

        // 조회된 주문을 DTO로 변환하여 반환
        return orderMapper.toDto(order.get());  // 주문 응답 DTO
    }

    // 주문 상태 변경 메서드
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

    // 사용자의 전체 주문 조회 메서드
    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);  // 사용자 ID로 주문 목록 조회
        return orders.stream()
                .map(orderMapper::toDto)  // Entity -> DTO로 변환
                .toList();
    }

    // 주문 취소 메서드
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문이 존재하지 않습니다."));

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }
}
