package com.playdata.productservice.client;


import com.playdata.productservice.common.auth.TokenUserInfo;
import com.playdata.productservice.review.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.AccessDeniedException;
import java.util.List;

@FeignClient(name = "ordering-service")
public interface OrderServiceClient {

    // 사용자의 전체 주문 조회 (email로 조회)
    @GetMapping("/orders//userOrderServer")
    public List<OrderResponseDto> getOrdersServer(@RequestParam String email ) throws AccessDeniedException ;

}
