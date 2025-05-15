package com.playdata.productservice.product.dto;

import com.playdata.productservice.product.entity.Product;
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
    private Long categoryId;
    private String categoryName;

    private List<String> productImages;

    public static ProductResDto fromEntity(Product product) {
        return ProductResDto.builder()
                .id(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .stockQuantity(product.getStockQuantity())
                .mainImagePath(product.getMainImagePath())
                .thumbnailPath(product.getThumbnailPath())
                .categoryId(product.getCategory().getCategoryId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}

