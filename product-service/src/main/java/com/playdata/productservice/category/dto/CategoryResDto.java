package com.playdata.productservice.category.dto;

import com.playdata.productservice.category.entity.Category;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResDto {
    private Long categoryId;
    private String categoryName;
    private String categoryBgImgUrl;

    public static CategoryResDto fromEntity() {
        return CategoryResDto.builder()

                .build();
    }
}
