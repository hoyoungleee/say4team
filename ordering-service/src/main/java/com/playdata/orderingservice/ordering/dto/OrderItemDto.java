package com.playdata.orderingservice.ordering.dto;

import lombok.*;

import java.math.BigDecimal;

@ToString
@NoArgsConstructor
@Getter
@Setter
public class OrderItemDto {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
    private String productName;
    private String mainImagePath;
    private String categoryName;

    public OrderItemDto(Long productId, int quantity, BigDecimal unitPrice, String productName, String mainImagePath, String categoryName) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.productName = productName;
        this.mainImagePath = mainImagePath;
        this.categoryName = categoryName;
    }
}
