package com.playdata.productservice.product.dto;

import com.playdata.productservice.product.entity.Category;
import com.playdata.productservice.product.entity.Product;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSaveReqDto {
    private String name;
    private String description;
    private int price;
    private int stockQuantity;
    //그냥 제일 먼저 나올 썸네일 이미지
    private MultipartFile thumbnailImage;
    //호버시 나올 대표이미지
    private MultipartFile mainImage;
    private List<MultipartFile> images;

    private Long categoryId; // 사용자가 선택한 카테고리 ID

    private String mainImagePath;
    private String thumbnailPath;

    private List<String> imageUrls;

    public Product toEntity(Category category) {
        return Product.builder()
                .name(name)
                .price(price)
                .description(description)
                .stockQuantity(stockQuantity)
                .mainImagePath(mainImagePath)
                .thumbnailPath(thumbnailPath)
                .category(category) // controller/service에서 주입
                .build();
    }
}








