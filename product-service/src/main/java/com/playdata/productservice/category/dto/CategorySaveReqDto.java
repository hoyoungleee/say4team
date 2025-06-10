package com.playdata.productservice.category.dto;

import com.playdata.productservice.category.entity.Category;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySaveReqDto {

    private String categoryName;

    private MultipartFile categoryBgImg;


    public Category toEntity(String categoryBgImgPath) {
        return Category.builder()
                .categoryName(categoryName)
                .categoryBgImgUrl(categoryBgImgPath)
                .build();
    }
}
