package com.playdata.productservice.product.controller;

import com.playdata.productservice.common.dto.CommonResDto;
import com.playdata.productservice.product.dto.ProductResDto;
import com.playdata.productservice.product.dto.ProductSaveReqDto;
import com.playdata.productservice.product.dto.ProductSearchDto;
import com.playdata.productservice.product.dto.ProductUpdateDto;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductContoller {

    private final ProductService productService;

    // 상품 등록 요청
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(ProductSaveReqDto dto)
            throws IOException {

        Product product = productService.productCreate(dto);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.CREATED, "상품 등록 성공", product.getProductId());

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listProduct(ProductSearchDto dto, Pageable pageable) {

        List<ProductResDto> dtoList = productService.productList(dto, pageable);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "상품 리스트 정상 조회", dtoList);

        return ResponseEntity.ok().body(resDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/update")
    public ResponseEntity<?> updateProduct(@RequestParam("id") Long id, ProductUpdateDto dto) throws Exception {
        log.info("/product/update: UPDATE, id: {} dto:{}", id, dto.toString());
        productService.productUpdate(dto, id);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "수정 완료", id);

        return ResponseEntity.ok().body(resDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(@RequestParam("id") Long id) throws Exception {
        log.info("/product/delete: DELETE, id: {}", id);
        productService.productDelete(id);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "삭제 완료", id);

        return ResponseEntity.ok().body(resDto);
    }

    // 단일 상품 조회
    @GetMapping("/detail/{prodId}")
    public ResponseEntity<?> getProductById(@PathVariable Long prodId) {

        ProductResDto dto = productService.getProductInfo(prodId);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "조회 완료", dto);

        return ResponseEntity.ok().body(resDto);
    }

    // 수량 업데이트
    @PutMapping("/updateQuantity")
    public ResponseEntity<?> updateStockQuantity(@RequestBody ProductResDto dto) {
        Long prodId = dto.getId();
        int stockQuantity = dto.getStockQuantity();
        log.info("/product/updateQuantity: PATCH, prodId: {}, stockQuantity: {}"
                , prodId, stockQuantity);
        productService.updateStockQuantity(prodId, stockQuantity);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "변경 완료", prodId);
        return ResponseEntity.ok().body(resDto);
    }

    // 한 사용자의 모든 주문 내역 안에 있는 상품 정보를 리턴하는 메서드
    @PostMapping("/products")
    public ResponseEntity<?> getProducts(@RequestBody List<Long> productIds) {
        log.info("/products: GET, productIds: {}", productIds);
        List<ProductResDto> productDtos = productService.getProductsName(productIds);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "조회 완료", productDtos);

        return ResponseEntity.ok().body(resDto);
    }

    // 주문 취소 시에 각 상품의 재고 수량을 원복하는 요청
    @PutMapping("/cancel")
    public ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map) {
        log.info("/product/cancel: PUT, map: {}", map);
        productService.cancelProduct(map);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "update completed", map);
        return ResponseEntity.ok().body(resDto);
    }

    /**
     * 카테고리 ID 목록에 해당하는 상품의 총 개수를 반환하는 API
     * POST 요청으로 categoryIds 리스트를 받습니다.
     * @param requestBody categoryIds를 포함하는 요청 바디 (예: { "categoryIds": [1, 2, 3] })
     * @return 상품 개수를 담은 응답 (예: { "count": 123 })
     */
    @PostMapping("/countByCategories")
    public ResponseEntity<?> countProductsByCategories(@RequestBody Map<String, List<Long>> requestBody) {
        log.info("POST /api/prod/countByCategories 요청 수신: {}", requestBody);
        List<Long> categoryIds = requestBody.get("categoryIds");

        if (categoryIds == null || categoryIds.isEmpty()) {
            // 요청 바디에 categoryIds가 없거나 비어있는 경우
            log.warn("요청 바디에 categoryIds가 없거나 비어있습니다.");
            return ResponseEntity.badRequest().body("categoryIds는 필수입니다.");
        }

        Long count = productService.countProductsByCategories(categoryIds);
        log.info("카테고리 {}에 해당하는 상품 총 개수: {}", categoryIds, count);

        return ResponseEntity.ok( count);
    }

}










