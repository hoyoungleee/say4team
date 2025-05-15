package com.playdata.productservice.review.dto;

import com.playdata.productservice.product.dto.ProductResDto;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.review.entity.Review;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResDto {
    private Long reviewId;
    private Long productId;
    private String content;
    private String mediaUrl;
    private String name;
    private String email;

    public static ReviewResDto fromEntity(Review review) {
        return ReviewResDto.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .content(review.getContent())
                .mediaUrl(review.getMediaUrl())
                .name(review.getUserName())
                .email(review.getUserEmail())
                .build();
    }
}
