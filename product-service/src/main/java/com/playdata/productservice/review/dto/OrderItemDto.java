package com.playdata.productservice.review.dto;

import lombok.*;

import java.math.BigDecimal;

@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class OrderItemDto {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
    private String productName;
    private String mainImagePath;
    private String categoryName;

}
