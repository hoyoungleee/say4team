package com.playdata.orderingservice.ordering.controller;

import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDto;
import com.playdata.orderingservice.ordering.dto.OrderRequestDto;
import com.playdata.orderingservice.ordering.dto.OrderResponseDto;
import com.playdata.orderingservice.ordering.dto.UpdateAddressRequest;
import com.playdata.orderingservice.ordering.entity.Order;
import com.playdata.orderingservice.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal TokenUserInfo userInfo, // 로그인된 사용자 정보
            @RequestBody OrderRequestDto orderRequestDto
    ) {
        log.info("/order/create: POST, userInfo: {}", userInfo);
        log.info("orderRequestDto: {}", orderRequestDto);

        // 이메일을 통해 로그인된 사용자의 정보를 전달하고 주문 생성
        Order order = orderService.createOrder(orderRequestDto, userInfo);

        CommonResDto resDto = new CommonResDto(
                HttpStatus.CREATED,
                "정상 주문 완료",
                order.getOrderId()
        );

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    // 사용자 전체 주문 취소
    @DeleteMapping("/{orderId}/cancel")
    public void deleteOrder(@PathVariable Long orderId, @AuthenticationPrincipal TokenUserInfo userInfo) throws AccessDeniedException {
        orderService.deleteOrder(orderId, userInfo);
    }

    // 주문 상세(상품) 상태 변경 (관리자,사용자) -> 개별 상품 관리 전용 기능
    @PutMapping("/items/{orderItemId}/status")
    public OrderResponseDto updateOrderItemStatus(
            @PathVariable Long orderItemId,
            @RequestParam String status,
            @AuthenticationPrincipal TokenUserInfo userInfo) throws AccessDeniedException {
        return orderService.updateOrderItemStatus(orderItemId, status, userInfo);
    }

    // 사용자의 전체 주문 조회 (email로 조회)
    @GetMapping("/userOrder")
    public List<OrderResponseDto> getOrders(@RequestParam String email, @AuthenticationPrincipal TokenUserInfo userInfo) throws AccessDeniedException {
        return orderService.getOrdersByEmail(email, userInfo);
    }

    // 관리자용: 전체 사용자 주문 전체 조회
    @GetMapping("/admin/all")
    public List<OrderResponseDto> getAllOrders(@AuthenticationPrincipal TokenUserInfo userInfo) throws AccessDeniedException {
        return orderService.getAllOrders(userInfo);
    }

    // 배송지 변경 요청 (주문 상태가 주문완료 상태일 때만 가능)
    @PatchMapping("/{orderId}/address")
    public ResponseEntity<?> updateOrderAddress(
            @PathVariable Long orderId,
            @RequestBody UpdateAddressRequest request,
            @AuthenticationPrincipal TokenUserInfo userInfo) throws AccessDeniedException {

        OrderResponseDto updatedOrder = orderService.updateOrderAddress(orderId, request.getAddress(), userInfo);

        CommonResDto resDto = new CommonResDto(
                HttpStatus.OK,
                "배송지 변경 완료",
                updatedOrder
        );

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

}
