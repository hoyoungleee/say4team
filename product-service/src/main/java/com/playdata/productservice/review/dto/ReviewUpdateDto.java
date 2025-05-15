package com.playdata.productservice.review.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateDto {
    private String content;
    private MultipartFile image;
}
