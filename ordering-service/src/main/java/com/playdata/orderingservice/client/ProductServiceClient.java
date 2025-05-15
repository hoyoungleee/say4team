package com.playdata.orderingservice.client;

import com.playdata.orderingservice.common.dto.CommonResDto;
import com.playdata.orderingservice.ordering.dto.ProductResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    // 상품 ID로 상품 정보를 조회하는 메서드
    @GetMapping("/product/{prodId}")
    CommonResDto<ProductResDto> findById(@PathVariable Long prodId);

    // 상품 수량 업데이트
    @PutMapping("/product/updateQuantity")
    ResponseEntity<?> updateQuantity(@RequestBody ProductResDto productResDto);

    // 여러 상품을 한 번에 조회하는 메서드
    @PostMapping("/product/products")
    CommonResDto<List<ProductResDto>> getProducts(@RequestBody List<Long> productIds);

    // 상품 취소 처리
    @PutMapping("/product/cancel")
    ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map);

    // 상품 정보를 가져오는 메서드 (단일 상품 조회)
    @GetMapping("/product/{productId}")
    ProductResDto getProductById(@PathVariable Long productId);

}
