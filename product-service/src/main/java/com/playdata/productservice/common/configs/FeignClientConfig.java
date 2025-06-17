package com.playdata.productservice.common.configs;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import com.playdata.productservice.common.auth.TokenUserInfo; // 실제 경로에 맞게 임포트

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestTokenInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 현재 스레드의 Security Context에서 인증 정보를 가져옵니다.
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof TokenUserInfo) {
                    TokenUserInfo userInfo = (TokenUserInfo) authentication.getPrincipal();

                    String userEmail = userInfo.getEmail();
                    // Enum 타입인 Role을 String으로 변환 (Role.ADMIN -> "ADMIN")
                    String userRole = userInfo.getRole() != null ? userInfo.getRole().toString() : null;

                    // 이메일과 역할 정보가 유효하면 HTTP 헤더에 추가
                    if (StringUtils.hasText(userEmail) && StringUtils.hasText(userRole)) {
                        template.header("X-User-Email", userEmail);
                        template.header("X-User-Role", userRole);
                        System.out.println("FeignClient RequestInterceptor: X-User-Email, X-User-Role 헤더 추가됨.");
                    } else {
                        System.out.println("FeignClient RequestInterceptor: TokenUserInfo에 이메일 또는 역할 정보가 비어있음.");
                    }
                } else {
                    System.out.println("FeignClient RequestInterceptor: SecurityContext에 인증 정보가 없거나 TokenUserInfo 타입이 아님.");
                }
            }
        };
    }
}