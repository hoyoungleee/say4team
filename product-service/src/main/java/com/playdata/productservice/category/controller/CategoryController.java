package com.playdata.productservice.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.playdata.productservice.category.dto.*;
import com.playdata.productservice.category.entity.Category;
import com.playdata.productservice.category.service.CategoryService;
import com.playdata.productservice.client.OrderServiceClient;
import com.playdata.productservice.client.UserServiceClient;
import com.playdata.productservice.common.auth.TokenUserInfo;
import com.playdata.productservice.common.dto.CommonResDto;
import com.playdata.productservice.review.dto.OrderResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final UserServiceClient userServiceClient;

    @GetMapping("/list")
    public ResponseEntity<?> getAllProductCategory(Pageable pageable) {
        List<CategoryResDto> productCategorys = categoryService.getAllProductCategory(pageable);
        if(productCategorys.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("카테고리가 없습니다.");
        }

        return ResponseEntity.ok(productCategorys);
    }

    @GetMapping("/navList")
    public ResponseEntity<?> getExtraProductCategory() {
        List<CategoryResDto> productCategorys = categoryService.getExtraProductCategory();
        if(productCategorys.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("추가 카테고리가 없습니다.");
        }

        return ResponseEntity.ok(productCategorys);
    }

    @GetMapping("/detail/{categoryId}")
    public ResponseEntity<?> getDetailProductCategory(@AuthenticationPrincipal TokenUserInfo tokenUserInfo, @PathVariable String categoryId) {
        if(!tokenUserInfo.getRole().toString().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("권한이 없습니다.");
        }
        CategoryResDto productCategory = categoryService.getDetailProductCategory(categoryId);
        if(productCategory == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("카테고리가 없습니다.");
        }

        return ResponseEntity.ok(productCategory);
    }


    @PostMapping("/create")
    public ResponseEntity<?> createProductCategory( @AuthenticationPrincipal TokenUserInfo tokenUserInfo, @ModelAttribute CategorySaveReqDto dto){
        if(!tokenUserInfo.getRole().toString().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("권한이 없습니다.");
        }
        try {
            ResponseEntity<?> productCategory = categoryService.createProductCategory(dto);

            return productCategory;
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청이 정상적으로 처리되지 못했습니다..");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProductCategory( @AuthenticationPrincipal TokenUserInfo tokenUserInfo, @ModelAttribute CategoryUpdateDto dto){
        if(tokenUserInfo == null || !tokenUserInfo.getRole().toString().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("권한이 없습니다.");
        }
        try {
            ResponseEntity<?> productCategory = categoryService.updateProductCategory(dto);

            return productCategory;
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청이 정상적으로 처리되지 못했습니다..");
        }
    }

    @DeleteMapping("/delete") // <-- URL에 PathVariable이 없으므로 이렇게 변경합니다.
    public ResponseEntity<?> deleteProductCategories( // 메서드 이름도 복수형으로 변경 (선택 사항)
                                                      @AuthenticationPrincipal TokenUserInfo tokenUserInfo,
                                                      @RequestBody CategoryDeleteRequestDto requestDto) { // <-- @RequestBody로 DTO를 받습니다.

        if (!tokenUserInfo.getRole().toString().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("권한이 없습니다.");
        }

        try {
            // DTO에서 ID 목록을 가져와 서비스 계층으로 전달합니다.
            categoryService.deleteProductCategories(requestDto.getCategoryIds());

            // 성공 응답 반환
            // 200 OK와 함께 메시지를 보내거나, 204 No Content를 반환할 수 있습니다.
            return ResponseEntity.ok("선택된 카테고리가 성공적으로 삭제되었습니다.");
            // 또는 return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 응답 본문 없음
        } catch (Exception e) {
            log.error("카테고리 삭제 실패: {}", e.getMessage(), e); // 스택 트레이스를 포함하여 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청이 정상적으로 처리되지 못했습니다.");
        }
    }


}
