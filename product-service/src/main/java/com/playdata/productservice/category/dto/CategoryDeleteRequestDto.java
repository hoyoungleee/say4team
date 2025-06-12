// product-service/src/main/java/com/playdata/productservice/category/dto/CategoryDeleteRequestDto.java

package com.playdata.productservice.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; // java.util.List 임포트

@Getter // Lombok: getter 메서드 자동 생성
@Setter // Lombok: setter 메서드 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자 자동 생성
public class CategoryDeleteRequestDto {
    private List<Long> categoryIds; // 프론트엔드에서 'categoryIds'라는 이름으로 보낸 데이터와 일치
}