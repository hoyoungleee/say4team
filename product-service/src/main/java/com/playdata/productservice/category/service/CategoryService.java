package com.playdata.productservice.category.service;

import com.playdata.productservice.category.dto.CategoryResDto;
import com.playdata.productservice.category.dto.CategorySaveReqDto;
import com.playdata.productservice.category.dto.CategoryUpdateDto;
import com.playdata.productservice.category.entity.Category;
import com.playdata.productservice.category.repository.CategoryRepository;
import com.playdata.productservice.common.configs.AwsS3Config;
import com.playdata.productservice.review.entity.Review;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AwsS3Config s3Config;

    public List<CategoryResDto> getAllProductCategory(Pageable pageable) {

        List<CategoryResDto> categoryList = categoryRepository.findAll(pageable)
                .stream().map(Category::fromEntity).collect(Collectors.toList());

        return categoryList;
    }

    public ResponseEntity<?> createProductCategory(CategorySaveReqDto reqDto) {

        if(reqDto.getCategoryBgImg() == null || reqDto.getCategoryBgImg().isEmpty()) {
            return ResponseEntity.internalServerError().body("필수 항목 이미지 등록 누락.");
        } else if(reqDto.getCategoryName() == null || reqDto.getCategoryName().isEmpty()) {
            return ResponseEntity.internalServerError().body("필수 항목 카테고리 이름 등록 누락.");
        }
        String categoryBgImageUrl = "";
        try {
            if(reqDto.getCategoryBgImg() != null) {
                MultipartFile categoryBgImg = reqDto.getCategoryBgImg();

                String uniqueReviewImageImageName
                        = UUID.randomUUID() + "_" + categoryBgImg.getOriginalFilename();
                categoryBgImageUrl = s3Config.uploadToS3Bucket(categoryBgImg.getBytes(), uniqueReviewImageImageName);

            }
            Category category = reqDto.toEntity(categoryBgImageUrl);
            categoryRepository.save(category);
        }catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("데이터 입력 실패.");
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("예상치 못한 에러. 관리자에게 문의바랍니다.");
        }

        return ResponseEntity.ok().body("카테고리 입력완료.");
    }

    public ResponseEntity<?> updateProductCategory(CategoryUpdateDto reqDto) {

        if(reqDto.getCategoryBgImg() == null || reqDto.getCategoryBgImg().isEmpty()) {
            return ResponseEntity.badRequest().body("필수 항목 이미지 등록 누락.");
        } else if(reqDto.getCategoryName() == null || reqDto.getCategoryName().isEmpty()) {
            return ResponseEntity.badRequest().body("필수 항목 카테고리 이름 등록 누락.");
        }

        Category findCategory = categoryRepository.findByCategoryId(reqDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));


        String categoryBgImageUrl = "";
        try {
            if(reqDto.getCategoryBgImg() != null) {
                String categoryBgImgUrl = findCategory.getCategoryBgImgUrl();
                s3Config.deleteFromS3Bucket(categoryBgImgUrl);

                MultipartFile categoryBgImg = reqDto.getCategoryBgImg();

                String uniqueReviewImageImageName
                        = UUID.randomUUID() + "_" + categoryBgImg.getOriginalFilename();
                categoryBgImageUrl = s3Config.uploadToS3Bucket(categoryBgImg.getBytes(), uniqueReviewImageImageName);

            }
            findCategory.setCategoryName(reqDto.getCategoryName());
            findCategory.setCategoryBgImgUrl(categoryBgImageUrl);
            categoryRepository.save(findCategory);
        }catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("데이터 수정 실패.");
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("예상치 못한 에러. 관리자에게 문의바랍니다.");
        }

        return ResponseEntity.ok().body("카테고리 수정완료.");
    }

    public ResponseEntity<String> deleteProductCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return ResponseEntity.badRequest().body("삭제할 카테고리 ID가 제공되지 않았습니다.");
        }

        try {
            for (Long categoryId : categoryIds) {
                Category findCategory = categoryRepository.findByCategoryId(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException(categoryId + "번 카테고리가 존재하지 않습니다."));

                String categoryBgImgUrl = findCategory.getCategoryBgImgUrl();

                if (categoryBgImgUrl != null && !categoryBgImgUrl.isEmpty()) {
                    s3Config.deleteFromS3Bucket(categoryBgImgUrl);
                }
                categoryRepository.deleteById(categoryId);
            }
            return ResponseEntity.ok().body("데이터 삭제 완료.");
        } catch (IllegalArgumentException e) {
            // Specific exception for category not found
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("S3 이미지 삭제 중 오류가 발생했습니다. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("예상치 못한 에러가 발생했습니다. 관리자에게 문의 바랍니다.");
        }
    }

    public CategoryResDto getDetailProductCategory(String categoryId) {
        Category category = categoryRepository.findByCategoryId(Long.parseLong(categoryId))
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 카테고리 아이디입니다."));

        CategoryResDto categoryResDto = Category.fromEntity(category);

        return categoryResDto;
    }

    public List<CategoryResDto> getExtraProductCategory() {
        List<CategoryResDto> categoryList = categoryRepository.findByExtraCategory()
                .stream().map(Category::fromEntity).collect(Collectors.toList());

        return categoryList;
    }
}
