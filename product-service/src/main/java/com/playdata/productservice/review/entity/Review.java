package com.playdata.productservice.review.entity;

import com.playdata.productservice.common.entity.BaseTimeEntity;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.review.dto.ReviewResDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_review")
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(length = 1000, nullable = false)
    private String mediaUrl;

    public static ReviewResDto fromEntity(Review review) {
        return ReviewResDto.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .content(review.getContent())
                .name(review.getUserName())
                .email(review.getUserEmail())
                .mediaUrl(review.getMediaUrl())
                .build();
    }
}
