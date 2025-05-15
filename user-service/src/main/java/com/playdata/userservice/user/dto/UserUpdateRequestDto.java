package com.playdata.userservice.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    private String name;
    private String address;
    private String phone;
    private String email;
    private String password;

}
