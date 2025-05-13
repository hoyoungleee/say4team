package com.playdata.userservice.user.dto;

import com.playdata.userservice.user.entity.Role;
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
