package com.playdata.productservice.client;

import com.playdata.productservice.review.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ordering-service")
public interface OrderServiceClient {

    // 사용자의 전체 주문 조회 (email로 조회)
    @GetMapping("/orders/userOrder")
    public List<OrderResponseDto> getOrdersServer(@RequestParam("email") String email);

}