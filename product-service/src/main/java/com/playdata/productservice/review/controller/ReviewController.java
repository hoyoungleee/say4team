package com.playdata.productservice.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.playdata.productservice.client.OrderServiceClient;
import com.playdata.productservice.client.UserServiceClient;
import com.playdata.productservice.common.auth.TokenUserInfo;
import com.playdata.productservice.common.dto.CommonResDto;
import com.playdata.productservice.review.dto.*;
import com.playdata.productservice.review.entity.Review;
import com.playdata.productservice.review.service.ReviewService;
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
@RequestMapping("/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;

    @GetMapping("/list/{prodId}")
    public ResponseEntity<?> reviewList(@PathVariable Long prodId, Pageable pageable) {


        List<ReviewResDto> reviews = reviewService.findByProdId(prodId, pageable);

        CommonResDto resDto = new CommonResDto(
                HttpStatus.OK,
                "정상 목록 호출 완료",
                reviews
        );

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }
    @GetMapping("/detail/{reviewId}")
    public ResponseEntity<?> reviewList(@PathVariable Long reviewId) {


        ReviewResDto reviews = reviewService.findById(reviewId);

        CommonResDto resDto = new CommonResDto(
                HttpStatus.OK,
                "정상 목록 호출 완료",
                reviews
        );

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @ModelAttribute ReviewSaveReqDto dto,
            @AuthenticationPrincipal TokenUserInfo tokenUserInfo,
            HttpServletRequest request
    ) throws IOException {

        //인증 정보 확인
        if (tokenUserInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        String email = tokenUserInfo.getEmail();
        String token = request.getHeader("Authorization");

        //Bearer 접두사 보정
        if (token != null && !token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }


        // 사용자의 주문 목록 조회
        List<OrderResponseDto> orders = orderServiceClient.getOrdersServer(email); // 토큰 필요하면 추가 파라미터

        // 1. 주문 목록이 null이거나 비어있으면 구매 이력이 없으므로 바로 거부
        if (orders == null || orders.isEmpty()) {
            return ResponseEntity.badRequest().body("구매한 상품만 리뷰 작성 가능합니다.");
        }

        System.out.println(dto.getProductId());
        // 2. 구매 이력 및 주문 아이템 상태(DELIVERED) 확인
        // 각 주문 아이템별로 상품 ID와 DELIVERED 상태를 동시에 확인합니다.
        boolean hasPurchasedAndDeliveredItem = orders.stream()
                // 모든 주문의 주문 아이템들을 하나의 스트림으로 평탄화
                .flatMap(order -> order.getOrderItems().stream())
                // 필터링: 현재 리뷰를 작성하려는 상품 ID와 일치하는 아이템만 선택
                .filter(item -> item.getProductId().equals(dto.getProductId()))
                // 필터링: 선택된 아이템의 상태가 'DELIVERED'인지 최종 확인
                // item.getOrderItemStatus()가 String 타입이라면:
                .anyMatch(item -> "DELIVERED".equalsIgnoreCase(item.getOrderStatus()));


        if (!hasPurchasedAndDeliveredItem) {
            return ResponseEntity.badRequest().body("배송 완료된 상품만 리뷰 작성 가능합니다.");
        }

        // 사용자 정보 조회
        ResponseEntity<?> userdata = userServiceClient.getUserByEmail(email, token);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        CommonResDto response = mapper.convertValue(userdata.getBody(), CommonResDto.class);
        UserResDto user = mapper.convertValue(response.getResult(), UserResDto.class);
        String name = user.getName();

        // 리뷰 저장
        Review review = reviewService.reviewCreate(dto, email, name);

        // 응답 구성
        CommonResDto resDto = new CommonResDto(
                HttpStatus.CREATED,
                "리뷰 생성 완료",
                review.getReviewId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
    }

    @PatchMapping("/update/{reviewId}")
    public ResponseEntity<?> reviewUpdate(@PathVariable Long reviewId, @ModelAttribute ReviewUpdateDto dto, @AuthenticationPrincipal TokenUserInfo tokenUserInfo) throws Exception {
        ReviewResDto byId = reviewService.findById(reviewId);
        String mediaUrl =byId.getMediaUrl();
        System.out.println("mediaUrl = " + mediaUrl);
        String userEmail = tokenUserInfo.getEmail();
        String authorEmail = byId.getEmail();
        if(!userEmail.equals(authorEmail)) {
            return ResponseEntity.badRequest().body("글 주인 다름");
        }else{
            reviewService.updateById(reviewId, dto);
            return ResponseEntity.ok().body("리뷰 수정 성공");
        }
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> reviewDelete(@PathVariable Long reviewId, @AuthenticationPrincipal TokenUserInfo tokenUserInfo) throws Exception {
        ReviewResDto byId = reviewService.findById(reviewId);
        String userEmail = tokenUserInfo.getEmail();
        String authorEmail = byId.getEmail();
        if(!userEmail.equals(authorEmail)) {
            return ResponseEntity.badRequest().body("글 주인 다름");
        }else{
            if(byId.getMediaUrl()!=null && !byId.getMediaUrl().equals("")) {

                reviewService.deleteById(reviewId, byId.getMediaUrl());
            }else {
                reviewService.deleteById(reviewId);
            }

            return ResponseEntity.ok().body("리뷰 삭제 성공");
        }
    }

}
