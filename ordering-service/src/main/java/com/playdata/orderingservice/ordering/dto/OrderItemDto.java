package com.playdata.orderingservice.ordering.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Builder
@Data
public class OrderItemDto {
    private Long productId;  // 제품 ID
    private int quantity;    // 수량
    private BigDecimal unitPrice;  // 제품 가격

    // unitPrice의 getter가 Lombok @Data 애너테이션에 의해 자동으로 생성됨
}
