package com.playdata.orderingservice.ordering.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResDto {

    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String thumbnailUrl;

}
