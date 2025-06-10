package com.playdata.userservice.user.controller;

import com.playdata.userservice.common.auth.JwtTokenProvider;
import com.playdata.userservice.common.auth.TokenRefreshRequestDto;
import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.common.dto.CommonResDto;
import com.playdata.userservice.user.dto.KakaoUserDto;
import com.playdata.userservice.user.dto.*;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@RefreshScope // spring cloud config가 관리하는 파일의 데이터가 변경되면 빈들을 새로고침해주는 어노테이션
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;


    private final Environment env;

    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@Valid @RequestBody UserSaveReqDto dto,
                                        @RequestParam(required = false, defaultValue = "user") String role) {

        dto.setRole(role);
        User saved = userService.userCreate(dto);

        CommonResDto resDto
                = new CommonResDto(HttpStatus.CREATED,
                "User Created", saved.getName());

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody UserLoginReqDto dto) {
        User user = userService.login(dto);
        String token
                = jwtTokenProvider.createToken(user.getEmail(), user.getRole().toString());


        String refreshToken
                = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole().toString());
        redisTemplate.opsForValue().set("user:refresh:" + user.getUserId(), refreshToken, 7, TimeUnit.MINUTES);


        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", token);
        loginInfo.put("email", user.getEmail());
        loginInfo.put("phone", user.getPhone());
        loginInfo.put("address", user.getAddress());
        loginInfo.put("role", user.getRole().toString());
        loginInfo.put("id", user.getUserId());

        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK,
                "Login Success", loginInfo);
        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getUserList(Pageable pageable) {
        List<UserResDto> dtoList = userService.userList(pageable);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "userList 조회 성공", dtoList);

        return ResponseEntity.ok().body(resDto);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable("id") String  id) {
        try {
            Long userId = Long.parseLong(id);
            User user = userService.findById(userId);
            return ResponseEntity.ok().body(user);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("ID 형식이 잘못되었습니다.");
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDto dto
    ) {
        User updatedUser = userService.updateUser(userId, dto);

        return ResponseEntity.ok().body(new CommonResDto(
                HttpStatus.OK,
                "회원정보가 수정되었습니다.",
                updatedUser
        ));
    }

    @PatchMapping("/address/{userId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long userId,
            @RequestBody UserAddressUpdateDto dto
    ) {
        User updatedUser = userService.updateUserAddress(userId, dto.getAddress());

        return ResponseEntity.ok().body(new CommonResDto(
                HttpStatus.OK,
                "주소가 수정되었습니다.",
                updatedUser
        ));
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new CommonResDto(
                HttpStatus.OK, "회원 탈퇴 완료", Collections.singletonMap("deleted", true)
        ));

    }

    @PutMapping("/restore/{userId}")
    public ResponseEntity<?> restoreUser(@PathVariable Long userId) {
        userService.restoreUser(userId);
        return ResponseEntity.ok(new CommonResDto(
                HttpStatus.OK, "회원 복구 완료", null
        ));
    }


    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequestDto requestDto) {
        try {
            TokenUserInfo userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(requestDto.getRefreshToken());
            String savedToken = (String) redisTemplate.opsForValue().get(userInfo.getEmail());

            if (!requestDto.getRefreshToken().equals(savedToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Refresh Token mismatch");
            }

            String newAccessToken = jwtTokenProvider.createToken(userInfo.getEmail(), userInfo.getRole().name());

            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", newAccessToken);
            return ResponseEntity.ok(tokenMap);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Refresh Token: " + e.getMessage());
        }
    }
    @GetMapping("/findByEmail")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("getUserByEmail: email: {}", email);
        UserResDto dto = userService.findByEmail(email);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "이메일로 회원 조회 완료", dto);
        return ResponseEntity.ok().body(resDto);
    }

    @PostMapping("/email-valid")
    public ResponseEntity<?> emailVaild(@RequestBody Map<String, String> map) {
        String email = map.get("email");
        log.info("이메일 인증 요청!: {}", map.get("email"));
        String authNum = userService.mailCheck(email);
        return ResponseEntity.ok().body(authNum);
    }

    //인증코드 검증 요청
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> map) {
        log.info("인증코드검증! map:{}",map);
        Map<String, String> result = userService.verifyEmail(map);
        return ResponseEntity.ok().body("인증 성공!");

    }

    //카카오 콜백 요청 처리
    @GetMapping("/kakao")
    public void kakaoCallback(@RequestParam String code ,
                              //응답을 평소처럼 주는게 아니라, 직접 커스텀 해서 클라이언트에게 전달
                              HttpServletResponse response) throws IOException {
        log.info("카카오 콜백 처리 시작! code: {}", code);


        //인가 코드로 엑세스토큰받기
        String kakaoAccessToken = userService.getKakaoAccessToken(code);
        //엑세스 토큰으로 사용자 정보받기
        KakaoUserDto dto = userService.getKakaoUserInfo(kakaoAccessToken);
        // 회원가입 or 로그인 처리
        UserResDto resDto = userService.findOrCreateKakaoUser(dto);

        //JWT 토큰 생성( 우리 사이트 로그인 유지를 위해. 사용자 정보를 위해.)
        String token = jwtTokenProvider.createToken(resDto.getEmail(), resDto.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(resDto.getEmail(), resDto.getRole().name());

        //redis에 저장
        redisTemplate.opsForValue().set("user:refresh:" + resDto.getUserid(), refreshToken, 2, TimeUnit.MINUTES);

        //팝업 닫기
        String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>카카오 로그인 완료</title></head>
                <body>
                    <script>
                        if (window.opener) {
                            window.opener.postMessage({
                                type: 'OAUTH_SUCCESS',
                                token: '%s',
                                id: '%s',
                                role: '%s',
                                provider: 'KAKAO'
                            }, 'http://localhost:9090');
                            window.close();
                        } else {
                            window.location.href = 'http://localhost:9090';
                        }
                    </script>
                    <p>카카오 로그인 처리 중...</p>
                </body>
                </html>
                """,
                token, resDto.getUserid(), resDto.getRole().toString());
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(html);
    }


    @GetMapping("/health-check")
    public String healthCheck() {
        String msg = "It's Working in User-service!\n";
        msg += "token.expiration_time: " + env.getProperty("token.expiration_time");
        msg += "token.secret: " + env.getProperty("token.secret");
        msg += "aws.accessKey: " + env.getProperty("aws.accessKey");
        msg += "aws.secretKey: " + env.getProperty("aws.secretKey");
        msg += "message: " + env.getProperty("message");


        return msg;
    }



}








