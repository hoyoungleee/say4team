package com.playdata.orderingservice.cart.dto;

import com.playdata.orderingservice.cart.entity.Cart;
import com.playdata.orderingservice.cart.entity.CartItem;
import com.playdata.orderingservice.ordering.dto.ProductResDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class CartResponseDto {

    private String email;
    private List<CartItemDetailDto> items;
    private BigDecimal totalPrice;

    public static CartResponseDto from(Cart cart, Map<Long, ProductResDto> productMap) {
        List<CartItemDetailDto> itemDtos = cart.getItems().stream()
                .map(item -> {
                    ProductResDto product = productMap.get(item.getProductId());
                    String name = product != null ? product.getName() : "Unknown Product";
                    BigDecimal unitPrice = product != null ? BigDecimal.valueOf(product.getPrice()) : BigDecimal.ZERO;
                    String imageUrl = product != null ? product.getThumbnailPath() : null;

                    return CartItemDetailDto.builder()
                            .productId(item.getProductId())
                            .productName(name)
                            .quantity(item.getQuantity())
                            .unitPrice(unitPrice)
                            .totalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal total = itemDtos.stream()
                .map(CartItemDetailDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDto.builder()
                .email(cart.getEmail())
                .items(itemDtos)
                .totalPrice(total)
                .build();
    }

    @Data
    @Builder
    public static class CartItemDetailDto {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String imageUrl;
    }

}
