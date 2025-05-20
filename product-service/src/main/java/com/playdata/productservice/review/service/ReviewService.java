package com.playdata.productservice.review.service;

import com.playdata.productservice.common.configs.AwsS3Config;
import com.playdata.productservice.review.dto.ReviewResDto;
import com.playdata.productservice.review.dto.ReviewSaveReqDto;
import com.playdata.productservice.review.dto.ReviewUpdateDto;
import com.playdata.productservice.review.entity.Review;
import com.playdata.productservice.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AwsS3Config s3Config;

    public List<ReviewResDto> findByProdId(Long prodId, Pageable pageable) {

        Page<Review> allByProductId = reviewRepository.findAllByProductId(prodId, pageable);

        List<Review> reviewList = allByProductId .getContent();

        return reviewList .stream()
                .map(ReviewResDto::fromEntity)
                .collect(Collectors.toList());

    }

    public Review reviewCreate(ReviewSaveReqDto dto, String email, String name) throws IOException {


        String mainImageUrl = "";
        if(dto.getImage() != null) {
            MultipartFile reviewImage = dto.getImage();

            String uniqueReviewImageImageName
                    = UUID.randomUUID() + "_" + reviewImage.getOriginalFilename();
            mainImageUrl = s3Config.uploadToS3Bucket(reviewImage.getBytes(), uniqueReviewImageImageName);

        }
        Review review = dto.toEntity(email, name, mainImageUrl);

        return reviewRepository.save(review);
    }

    public ReviewResDto findById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new IllegalArgumentException("Review not found")
        );
        ReviewResDto reviewResDto = review.fromEntity(review);
        return reviewResDto;
    }

    public void deleteById(Long reviewId, String imgUrl) throws Exception {
        s3Config.deleteFromS3Bucket(imgUrl);
        reviewRepository.deleteById(reviewId);
    }
    public void deleteById(Long reviewId) throws Exception {
        reviewRepository.deleteById(reviewId);
    }

    public void updateById(Long reviewId, String imgUrl, ReviewUpdateDto dto) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰 없음"));

        // content가 유효한 경우만 업데이트
        if (dto.getContent() != null && !dto.getContent().trim().isEmpty()) {
            review.setContent(dto.getContent());
        }

        // 이미지가 있을 경우 기존 삭제 + 새 이미지 등록
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            s3Config.deleteFromS3Bucket(imgUrl); // 기존 이미지 삭제
            String newImageUrl = s3Config.uploadToS3Bucket(
                    dto.getImage().getBytes(),
                    UUID.randomUUID() + "_" + dto.getImage().getOriginalFilename()
            );
            review.setMediaUrl(newImageUrl);
        }

        reviewRepository.save(review); // 저장
    }
}
