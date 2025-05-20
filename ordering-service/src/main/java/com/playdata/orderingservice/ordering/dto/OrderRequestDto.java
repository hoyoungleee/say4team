package com.playdata.orderingservice.ordering.dto;

import lombok.Data;
import java.util.List;


// 주문 생성 요청 DTO

@Data
public class OrderRequestDto {
    private List<Long> cartItemIds; // 선택된 카트 아이템 ID만 전달
}

