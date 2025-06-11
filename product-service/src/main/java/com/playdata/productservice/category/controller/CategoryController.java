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
//        // 사용자 정보 조회
//        ResponseEntity<?> userdata = userServiceClient.getUserByEmail(email, token);

    @GetMapping("/list")
    public ResponseEntity<?> getAllProductCategory(Pageable pageable) {
        List<CategoryResDto> productCategorys = categoryService.getAllProductCategory(pageable);
        if(productCategorys.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("카테고리가 없습니다.");
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

    @PostMapping("/update")
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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProductCategory(@AuthenticationPrincipal TokenUserInfo tokenUserInfo, String categoryId){
        if(!tokenUserInfo.getRole().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("권한이 없습니다.");
        }
        try {
            ResponseEntity<?> productCategory = categoryService.deleteProductCategory(categoryId);

            return productCategory;
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청이 정상적으로 처리되지 못했습니다..");
        }
    }


}
