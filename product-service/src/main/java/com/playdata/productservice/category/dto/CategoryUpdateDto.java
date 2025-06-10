package com.playdata.productservice.category.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateDto {
    //수정할땐 참조해야할 아이디가 있어야함.
    private Long categoryId;

    private String categoryName;

    private MultipartFile categoryBgImg;
}
