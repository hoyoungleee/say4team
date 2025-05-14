package com.playdata.orderingservice.cart.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private List<CartItemDto> items;
}
