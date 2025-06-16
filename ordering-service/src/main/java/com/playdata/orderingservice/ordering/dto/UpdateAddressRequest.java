package com.playdata.orderingservice.ordering.dto;

import lombok.Data;
import lombok.Setter;

@Data
public class UpdateAddressRequest {
    @Setter
    private String address;
}
