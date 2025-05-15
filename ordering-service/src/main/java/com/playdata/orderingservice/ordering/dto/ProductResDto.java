package com.playdata.orderingservice.ordering.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResDto {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String mainImagePath;
    private String thumbnailPath;
    private String description;
    private String categoryName;

    private List<String> productImages;

}