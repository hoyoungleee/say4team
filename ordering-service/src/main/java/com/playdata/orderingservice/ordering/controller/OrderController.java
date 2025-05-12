package com.playdata.orderingservice.ordering.controller;

import com.playdata.orderingservice.ordering.dto.OrderRequestDto;
import com.playdata.orderingservice.ordering.dto.OrderResponseDto;
import com.playdata.orderingservice.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        log.info("주문 생성 요청: {}", orderRequestDto);
        return orderService.createOrder(orderRequestDto);
    }

    // 주문 조회
    @GetMapping("/{orderId}")
    public OrderResponseDto getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    // 사용자의 전체 주문 조회
    @GetMapping("/userOrder")
    public List<OrderResponseDto> getOrders(@RequestParam Long userId) {
        log.info("사용자의 전체 주문 조회: {}", userId);
        return orderService.getOrdersByUser(userId);
    }

    // 주문 상태 업데이트
    @PutMapping("/{orderId}/status")
    public OrderResponseDto updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @DeleteMapping("/{orderId}/cancel")
    public void deleteOrder(@PathVariable Long orderId) {
        log.info("주문 취소 요청: {}", orderId);
        orderService.deleteOrder(orderId);
    }


}
