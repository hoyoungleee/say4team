package com.playdata.productservice.review.dto;

import com.playdata.productservice.product.entity.Category;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.review.entity.Review;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSaveReqDto {
    private Long productId;
    private String content;
    private MultipartFile image;

    public Review toEntity(String userEmail,String userName, String imagePath) {
        return Review.builder()
                .productId(productId)
                .content(content)
                .mediaUrl(imagePath)
                .userEmail(userEmail)
                .userName(userName)
                .build();
    }
}
