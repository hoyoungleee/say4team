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
import com.playdata.orderingservice.ordering.repository.OrderItemRepository;
import com.playdata.orderingservice.ordering.repository.OrderRepository;
import com.playdata.orderingservice.cart.service.CartService;
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
    private final OrderItemRepository orderItemRepository;

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
        List<CartResponseDto.CartItemDetailDto> allCartItems = cartResponse.getItems();

        // 2. 요청된 cartItemIds만 필터링
        List<Long> selectedCartItemIds = orderRequestDto.getCartItemIds();

        // null 체크 추가
        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            throw new IllegalArgumentException("선택된 장바구니 아이템이 없습니다.");
        }

        List<CartResponseDto.CartItemDetailDto> selectedCartItems = allCartItems.stream()
                .filter(item -> selectedCartItemIds.contains(item.getCartItemId()))
                .collect(Collectors.toList());

        // 3. 상품 ID 목록 추출
        List<Long> productIds = selectedCartItems.stream()
                .map(CartResponseDto.CartItemDetailDto::getProductId)
                .collect(Collectors.toList());

        // 4. 상품 정보 조회 (가격 포함)
        List<ProductResDto> productList = getProductsByIds(productIds);
        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        // 5. 주문 항목 생성
        List<OrderItem> orderItems = selectedCartItems.stream()
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

        // 6. 총 가격 계산
        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. 주문 생성
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

        // 8. 저장
        orderRepository.save(order);

        // 9. 장바구니에서 주문한 아이템만 삭제
        cartService.removeCartItems(tokenUserInfo, selectedCartItemIds);


        // 10. 상품 수량 감소 요청
        selectedCartItems.forEach(cartItem -> {
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

        // 11. 주문 상태 업데이트
        order.setOrderStatus(OrderStatus.ORDERED); // 주문 완료 상태로 변경
        // 각 orderItem의 상태도 변경
        orderItems.forEach(item -> item.setOrderStatus(OrderStatus.ORDERED));
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

    // 사용자 전체 주문 취소
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

        // 1. 주문 항목별 상품ID와 수량을 Map에 담아서 재고 수량 증가 요청
        Map<Long, Integer> cancelMap = new HashMap<>();
        for (OrderItem item : order.getOrderItems()) {
            cancelMap.put(item.getProductId(), item.getQuantity());
        }

        try {
            productServiceClient.cancelProduct(cancelMap);
        } catch (Exception e) {
            log.error("상품 재고 수량 증가 실패: {}", e.getMessage());
            throw new RuntimeException("상품 재고 수량 증가 실패");
        }

        // 2. 주문 항목 상태 모두 CANCELED로 변경 및 저장
        for (OrderItem item : order.getOrderItems()) {
            item.setOrderStatus(OrderStatus.CANCELED);
            orderItemRepository.save(item);
        }

        // 3. 주문 상태 CANCELED로 변경 및 저장
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    // 관리자 페이지 전용 주문 관리 기능
    public List<OrderResponseDto> getAllOrders(TokenUserInfo userInfo) throws AccessDeniedException {
        if (!isAdmin(userInfo)) {
            throw new AccessDeniedException("관리자만 전체 주문을 조회할 수 있습니다.");
        }

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.CANCELED)
                .collect(Collectors.toList());

        List<Long> productIds = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResDto> productList = getProductsByIds(productIds);

        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        return orders.stream()
                .map(order -> orderMapper.toDto(order, productMap))
                .collect(Collectors.toList());
    }

    // 개별 상품 취소
    public OrderResponseDto updateOrderItemStatus(Long orderItemId, String status, TokenUserInfo tokenUserInfo) throws AccessDeniedException {
        // 1. 주문 항목 조회
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 주문 항목이 존재하지 않습니다. ID: " + orderItemId));

        // 2. 주문 가져오기
        Order order = orderItem.getOrder();

        // 3. 권한 체크 (관리자 또는 본인 주문인지)
        if (!isAdmin(tokenUserInfo) && !order.getEmail().equals(tokenUserInfo.getEmail())) {
            throw new AccessDeniedException("자기 자신의 주문 항목만 변경할 수 있습니다.");
        }

        // 4. 주문 상태 유효성 검사 및 Enum 변환
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 주문 상태입니다: " + status);
        }

        // 5. 이미 같은 상태일 경우 예외 처리
        if (orderItem.getOrderStatus() == newStatus) {
            throw new IllegalStateException("이미 해당 상태로 설정되어 있습니다.");
        }

        // 6. 주문 항목 상태 변경
        orderItem.setOrderStatus(newStatus);
        orderItemRepository.save(orderItem);

        // 6-1. 주문 상품이 취소 상태로 변경되면 재고 수량 증가 처리
        if (newStatus == OrderStatus.CANCELED) {
            Map<Long, Integer> cancelMap = new HashMap<>();
            cancelMap.put(orderItem.getProductId(), orderItem.getQuantity());

            try {
                productServiceClient.cancelProduct(cancelMap);
            } catch (Exception e) {
                log.error("상품 재고 수량 증가 실패: {}", e.getMessage());
                throw new RuntimeException("상품 재고 수량 증가 실패");
            }
        }

        // 7. 전체 주문 상태 업데이트 로직 (예: 모든 항목 취소시 주문 상태도 변경)
        // 최신 상태로 주문 항목 리스트를 DB에서 다시 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(order.getOrderId());

        boolean allCanceled = orderItems.stream()
                .allMatch(item -> item.getOrderStatus() == OrderStatus.CANCELED);

        if (allCanceled) {
            order.setOrderStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        }

        // 8. 변경된 주문 정보를 반환 (상품 정보 포함)
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductResDto> productList = getProductsByIds(productIds);
        Map<Long, ProductResDto> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));

        return orderMapper.toDto(order, productMap);
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


}
