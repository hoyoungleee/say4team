package com.playdata.orderingservice.ordering.service;

import com.playdata.orderingservice.client.ProductServiceClient;
import com.playdata.orderingservice.client.UserServiceClient;
import com.playdata.orderingservice.common.auth.Role;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDto;
import com.playdata.orderingservice.ordering.dto.*;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.entity.OrderItem;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import com.playdata.orderingservice.ordering.mapper.OrderMapper;
import com.playdata.orderingservice.ordering.repository.OrderRepository;
import com.playdata.orderingservice.cart.service.CartService;
import com.playdata.orderingservice.cart.dto.CartItemDto;
import com.playdata.orderingservice.cart.dto.CartResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final CartService cartService;

    // 주문 생성
    public Order createOrder(OrderRequestDto orderRequestDto, TokenUserInfo tokenUserInfo) {
        String userEmail = tokenUserInfo.getEmail();
        if (userEmail == null) {
            throw new RuntimeException("토큰에서 사용자 정보를 가져올 수 없습니다.");
        }

        // 사용자 정보 조회 (주소 포함)
        CommonResDto<UserResDto> userResponse = userServiceClient.findByEmail(userEmail);
        if (userResponse == null || userResponse.getResult() == null) {
            throw new RuntimeException("사용자 정보가 없습니다.");
        }
        String address = userResponse.getResult().getAddress();

        // 1. 장바구니 조회
        CartResponseDto cartResponse = cartService.getCart(tokenUserInfo);
        List<CartResponseDto.CartItemDetailDto> cartItems = cartResponse.getItems();

        // 2. 상품 ID 목록 추출
        List<Long> productIds = cartItems.stream()
                .map(CartResponseDto.CartItemDetailDto::getProductId)
                .collect(Collectors.toList());

        // 3. 상품 정보 조회 (가격 포함)
        List<ProductResDto> productList = getProductsByIds(productIds);
        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        // 4. 주문 항목 생성
        List<OrderItem> orderItems = cartItems.stream()
                .map(dto -> {
                    ProductResDto product = productMap.get(dto.getProductId());
                    if (product == null) {
                        throw new RuntimeException("상품 정보를 찾을 수 없습니다. ID: " + dto.getProductId());
                    }
                    return OrderItem.builder()
                            .productId(dto.getProductId())
                            .quantity(dto.getQuantity())
                            .unitPrice(BigDecimal.valueOf(product.getPrice()))
                            .build();
                })
                .collect(Collectors.toList());

        // 5. 총 가격 계산
        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. 주문 생성
        Order order = Order.builder()
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING_USER_FAILURE)
                .orderedAt(LocalDateTime.now())
                .email(userEmail)
                .address(address)
                .orderItems(orderItems)
                .build();

        // 양방향 관계 설정
        orderItems.forEach(item -> item.setOrder(order));

        // 7. 저장
        orderRepository.save(order);

        // 8. 장바구니 비우기
        cartService.clearCart(tokenUserInfo);

        // 9. 상품 수량 감소 요청
        cartItems.forEach(cartItem -> {
            ProductResDto product = productMap.get(cartItem.getProductId());
            if (product != null) {
                int newQuantity = product.getStockQuantity() - cartItem.getQuantity(); // 수량 차감
                product.setStockQuantity(newQuantity); // 상품 수량 업데이트

                // 상품 수량 업데이트 요청
                try {
                    productServiceClient.updateQuantity(product); // 수량 업데이트 호출
                } catch (Exception e) {
                    log.error("상품 수량 업데이트 실패: {}", e.getMessage());
                    throw new RuntimeException("상품 수량 업데이트 실패");
                }
            }
        });

        // 10. 주문 상태 업데이트
        order.setOrderStatus(OrderStatus.ORDERED); // 주문 완료 상태로 변경
        orderRepository.save(order); // 변경된 상태 저장

        return order;
    }

    // 사용자 전체 주문 조회
    public List<OrderResponseDto> getOrdersByEmail(String email, TokenUserInfo tokenUserInfo) throws AccessDeniedException {
        // 관리자 권한 체크
        if (!isAdmin(tokenUserInfo)) {
            // 사용자가 자신만의 주문을 조회할 수 있도록
            if (!email.equals(tokenUserInfo.getEmail())) {
                throw new AccessDeniedException("자기 자신의 주문만 조회할 수 있습니다.");
            }
        }

        List<Order> orders = orderRepository.findAllByEmail(email).stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.CANCELED)
                .collect(Collectors.toList());

        // 모든 주문에서 상품 ID만 추출
        List<Long> productIds = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // 상품 정보 조회
        List<ProductResDto> productList = getProductsByIds(productIds);

        // 상품 정보를 Map으로 변환 (ID -> ProductResDto)
        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        // 주문 DTO 반환
        return orders.stream()
                .map(order -> orderMapper.toDto(order, productMap)) // 상품 정보를 포함하여 변환
                .collect(Collectors.toList());
    }

    // 주문 단건 조회
    public OrderResponseDto getOrder(Long orderId, TokenUserInfo tokenUserInfo) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. 주문 ID: " + orderId));

        // 관리자 권한 체크
        if (!isAdmin(tokenUserInfo) && !order.getEmail().equals(tokenUserInfo.getEmail())) {
            throw new AccessDeniedException("자기 자신의 주문만 조회할 수 있습니다.");
        }

        // 주문에 포함된 상품 ID 추출
        List<Long> productIds = order.getOrderItems().stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());

        // 상품 정보 조회
        List<ProductResDto> productList = getProductsByIds(productIds);

        // 상품 정보를 Map으로 변환 (ID -> ProductResDto)
        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        return orderMapper.toDto(order, productMap); // 상품 정보를 포함하여 변환
    }

    // 주문 상태 업데이트
    public OrderResponseDto updateOrderStatus(Long orderId, String status, TokenUserInfo tokenUserInfo) throws AccessDeniedException {
        // 관리자 권한 체크
        if (!isAdmin(tokenUserInfo)) {
            throw new AccessDeniedException("관리자만 주문 상태를 변경할 수 있습니다.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. 주문 ID: " + orderId));

        // 주문 상태 값이 유효한지 확인
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status); // 문자열을 Enum으로 변환
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 주문 상태입니다: " + status);
        }

        // 상태 업데이트
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        return getOrder(orderId, tokenUserInfo); // 변경 후 최신 데이터 반환
    }

    // 주문 취소
    public void deleteOrder(Long orderId, TokenUserInfo tokenUserInfo) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 주문이 존재하지 않습니다."));

        // 관리자 권한 체크 또는 주문이 본인 것인지 확인
        if (!isAdmin(tokenUserInfo) && !order.getEmail().equals(tokenUserInfo.getEmail())) {
            throw new AccessDeniedException("자기 자신의 주문만 취소할 수 있습니다.");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    // 관리자 여부 확인(공통 메서드로 빼놈)
    private boolean isAdmin(TokenUserInfo tokenUserInfo) {
        return Role.ADMIN.equals(tokenUserInfo.getRole());
    }

    // 상품 정보를 여러 개 조회하는 공통 메서드
    private List<ProductResDto> getProductsByIds(List<Long> productIds) {
        // 여러 상품 정보 조회
        CommonResDto<List<ProductResDto>> productResponse = productServiceClient.getProducts(productIds);

        if (productResponse == null || productResponse.getResult() == null) {
            throw new RuntimeException("상품 정보 조회 실패");
        }

        return productResponse.getResult(); // 상품 정보 반환
    }

    public List<OrderResponseDto> getOrdersByEmailServer(String email) {

        List<Order> orders = orderRepository.findAllByEmail(email).stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.CANCELED)
                .collect(Collectors.toList());

        // 모든 주문에서 상품 ID만 추출
        List<Long> productIds = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // 상품 정보 조회
        List<ProductResDto> productList = getProductsByIds(productIds);

        // 상품 정보를 Map으로 변환 (ID -> ProductResDto)
        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        // 주문 DTO 반환
        return orders.stream()
                .map(order -> orderMapper.toDto(order, productMap)) // 상품 정보를 포함하여 변환
                .collect(Collectors.toList());
    }
}
