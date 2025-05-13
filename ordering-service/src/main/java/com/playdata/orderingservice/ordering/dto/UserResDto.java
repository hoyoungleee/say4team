package com.playdata.orderingservice.ordering.dto;

import com.playdata.orderingservice.common.auth.Role;
import com.playdata.orderingservice.common.entity.Address;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResDto {

    private Long userid;
    private String email;
    private String name;
    private Role role;
    private String address;
    private String phone;
    private LocalDate birthdate;

}
