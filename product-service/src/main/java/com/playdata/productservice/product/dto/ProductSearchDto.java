package com.playdata.productservice.product.dto;

import lombok.*;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchDto {

    private String searchType;
    private Long categoryId;
    private String searchName;

}
