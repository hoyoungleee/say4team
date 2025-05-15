package com.playdata.productservice.client;


import com.playdata.productservice.common.auth.TokenUserInfo;
import com.playdata.productservice.review.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.AccessDeniedException;
import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/user/findByEmail")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email, @RequestHeader("Authorization") String token);
}
