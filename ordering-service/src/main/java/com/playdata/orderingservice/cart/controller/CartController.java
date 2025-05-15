package com.playdata.orderingservice.cart.controller;

import com.playdata.orderingservice.cart.dto.CartItemDto;
import com.playdata.orderingservice.cart.dto.CartResponseDto;
import com.playdata.orderingservice.cart.dto.QuantityUpdateDto;
import com.playdata.orderingservice.cart.service.CartService;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor

public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping("/details")
    public CartResponseDto getCart(@AuthenticationPrincipal TokenUserInfo tokenUserInfo) {
        return cartService.getCart(tokenUserInfo);
    }

    // 장바구니에 상품 추가
    @PostMapping("/items")
    public CartResponseDto addItem(@AuthenticationPrincipal TokenUserInfo tokenUserInfo,
                                   @RequestBody CartItemDto dto) {
        return cartService.addItemToCart(dto, tokenUserInfo);
    }

    // 장바구니에서 상품 제거
    @DeleteMapping("/items/{productId}")
    public CartResponseDto removeItem(@AuthenticationPrincipal TokenUserInfo tokenUserInfo,
                                      @PathVariable Long productId) {
        return cartService.removeItemFromCart(productId, tokenUserInfo);
    }

    // 장바구니 비우기
    @DeleteMapping("/clear")
    public void clearCart(@AuthenticationPrincipal TokenUserInfo tokenUserInfo) {
        cartService.clearCart(tokenUserInfo);
    }

    // 장바구니에서 상품 수량 수정
    @PatchMapping("/items/{productId}/quantity")
    public CartResponseDto updateItemQuantity(
            @AuthenticationPrincipal TokenUserInfo tokenUserInfo,
            @PathVariable Long productId,
            @RequestBody QuantityUpdateDto dto) {
        return cartService.updateItemQuantity(productId, dto.getQuantity(), tokenUserInfo);
    }

}
