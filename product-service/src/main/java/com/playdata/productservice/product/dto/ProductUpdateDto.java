package com.playdata.productservice.product.dto;

import com.playdata.productservice.product.entity.Category;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDto {
    private String name;
    private Integer price;
    private String description;
    private MultipartFile mainImage;
    private MultipartFile thumbnailImage;
    private List<MultipartFile> images;
    String categoryId;

}
