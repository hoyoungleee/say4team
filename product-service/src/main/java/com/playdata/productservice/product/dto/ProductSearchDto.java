package com.playdata.productservice.product.dto;

import lombok.*;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchDto {

    // ALL 혹은 카테고리 아이디 넘기기
    private String searchType;

    private String searchName;

}
