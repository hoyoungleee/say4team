package com.playdata.userservice.common.auth;

import com.playdata.userservice.user.entity.Role;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.entity.UserStatus;
import com.playdata.userservice.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String userEmail = request.getHeader("X-User-Email");
            String userRole = request.getHeader("X-User-Role");

            if (userEmail != null && userRole != null) {
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

                if (user.getStatus() != UserStatus.ACTIVE) {
                    log.warn("비활성화된 계정 접근 차단: {}", userEmail);
                    response.resetBuffer();  // 스트림 리셋
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"탈퇴하거나 정지된 계정입니다.\"}");
                    response.flushBuffer();
                    return;
                }

                // 인증 객체 생성
                List<SimpleGrantedAuthority> authorityList = List.of(
                        new SimpleGrantedAuthority("ROLE_" + userRole));

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        new TokenUserInfo(userEmail, Role.valueOf(userRole)),
                        null,
                        authorityList
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.warn("JWT 필터 인증 실패: {}", e.getMessage());
            if (!response.isCommitted()) {
                response.resetBuffer();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"인증 실패: 잘못된 토큰 또는 사용자 정보\"}");
                response.flushBuffer();
            }
        }
    }
}











