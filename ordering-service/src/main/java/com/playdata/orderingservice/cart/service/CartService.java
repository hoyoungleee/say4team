package com.playdata.orderingservice.cart.service;

import com.playdata.orderingservice.cart.dto.CartItemDto;
import com.playdata.orderingservice.cart.dto.CartResponseDto;
import com.playdata.orderingservice.cart.entity.Cart;
import com.playdata.orderingservice.cart.entity.CartItem;
import com.playdata.orderingservice.cart.repository.CartRepository;
import com.playdata.orderingservice.client.ProductServiceClient;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.common.dto.CommonResDto;
import com.playdata.orderingservice.ordering.dto.ProductResDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    // 장바구니 조회
    public CartResponseDto getCart(TokenUserInfo tokenUserInfo) {
        String email = tokenUserInfo.getEmail();
        Cart cart = cartRepository.findByEmail(email)
                .orElseGet(() -> createEmptyCart(email));

        Map<Long, ProductResDto> productMap = getProductMap(cart);

        return CartResponseDto.from(cart, productMap);
    }

    // 장바구니에 상품 추가
    public CartResponseDto addItemToCart(CartItemDto dto, TokenUserInfo tokenUserInfo) {
        String email = tokenUserInfo.getEmail();
        Cart cart = cartRepository.findByEmail(email)
                .orElseGet(() -> createEmptyCart(email));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(dto.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(dto.getQuantity());
        } else {
            CartItem item = CartItem.builder()
                    .productId(dto.getProductId())
                    .quantity(dto.getQuantity())
                    .cart(cart)
                    .build();
            cart.getItems().add(item);
        }

        Cart savedCart = cartRepository.save(cart);
        Map<Long, ProductResDto> productMap = getProductMap(savedCart);
        return CartResponseDto.from(savedCart, productMap);
    }

    // 장바구니에서 특정 상품 제거
    public CartResponseDto removeItemFromCart(Long productId, TokenUserInfo tokenUserInfo) {
        String email = tokenUserInfo.getEmail();
        Cart cart = cartRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("장바구니가 존재하지 않습니다."));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        Cart savedCart = cartRepository.save(cart);
        Map<Long, ProductResDto> productMap = getProductMap(savedCart);
        return CartResponseDto.from(savedCart, productMap);
    }

    // 장바구니 비우기
    public void clearCart(TokenUserInfo tokenUserInfo) {
        String email = tokenUserInfo.getEmail();
        Cart cart = cartRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("장바구니가 존재하지 않습니다."));
        cart.getItems().clear(); // 장바구니 항목 비움.
        cartRepository.save(cart); // 변경사항을 DB에 반영.
    }

    // 수량 업데이트
    public CartResponseDto updateItemQuantity(Long productId, int quantity, TokenUserInfo tokenUserInfo) {
        String email = tokenUserInfo.getEmail();
        Cart cart = cartRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("장바구니가 존재하지 않습니다."));

        CartItem targetItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 장바구니에 존재하지 않습니다."));

        if (quantity <= 0) {
            cart.getItems().remove(targetItem);
        } else {
            targetItem.setQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        Map<Long, ProductResDto> productMap = getProductMap(savedCart);
        return CartResponseDto.from(savedCart, productMap);
    }


    /* 공통 메서드 부분 */

    private Cart createEmptyCart(String email) {
        return Cart.builder()
                .email(email)
                .items(new ArrayList<>())
                .build();
    }

    private Map<Long, ProductResDto> getProductMap(Cart cart) {
        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        if (productIds.isEmpty()) return Collections.emptyMap();

        CommonResDto<List<ProductResDto>> productResponse = productServiceClient.getProducts(productIds);
        List<ProductResDto> productList = productResponse.getResult();

        return productList.stream()
                .collect(Collectors.toMap(ProductResDto::getId, p -> p));
    }
}
