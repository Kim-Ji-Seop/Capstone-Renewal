package com.uou.capstone.domain.app.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uou.capstone.domain.app.user.dto.*;
import com.uou.capstone.domain.app.user.service.UserService;
import com.uou.capstone.global.config.error.BaseResponse;
import com.uou.capstone.global.config.security.jwt.JwtAuthenticationFilter;
import com.uou.capstone.global.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class UserController {
    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtTokenProvider jwtTokenProvider;
    @GetMapping("/test")
    public String testApi(){
        return "Success";
    }
    // 1. 이메일 인증코드 전송
    @PostMapping("/users/auth/email/validation")
    public ResponseEntity<BaseResponse<PostAuthEmailRes>> emailCheck(@RequestBody PostAuthEmailReq postAuthEmailReq){
        PostAuthEmailRes postAuthEmailRes = userService.emailcheck(postAuthEmailReq);
        return ResponseEntity.ok(new BaseResponse<>(postAuthEmailRes));
    }

    // 2. 이메일 인증 회원가입
    @PostMapping("/users/auth/email")
    public ResponseEntity<BaseResponse<PostSignUpUserRes>> emailSignUp(@RequestBody PostSignUpUserReq postSignUpUserReq){
        PostSignUpUserRes postSignUpUserRes = userService.emailSignUp(postSignUpUserReq);
        return ResponseEntity.ok(new BaseResponse<>(postSignUpUserRes));
    }
    // 3. 이메일 로그인
    @PostMapping("/users/login/email")
    public ResponseEntity<BaseResponse<PostLoginEmailRes>> emailLogin(@RequestBody PostLoginEmailReq postLoginEmailReq){
        PostLoginEmailRes postLoginEmailRes = userService.emailLogin(postLoginEmailReq);
        return ResponseEntity.ok(new BaseResponse<>(postLoginEmailRes));
    }

    // 4. 카카오 로그인 (SDK)
    @PostMapping("/users/auth/kakao")
    public ResponseEntity<BaseResponse<PostAuthKakaoSdkRes>> kakaoSdkLogin(@RequestBody PostAuthKakaoSdkReq postAuthKakaoSdkReq){
        PostAuthKakaoSdkRes postAuthKakaoSdkRes = userService.kakaoSdkLogin(postAuthKakaoSdkReq);
        return ResponseEntity.ok(new BaseResponse<>(postAuthKakaoSdkRes));
    }

    // 5. 구글 로그인 (SDK)
    @PostMapping("/users/auth/google")
    public ResponseEntity<BaseResponse<PostAuthGoogleSdkRes>> googleSdkLogin(@RequestBody PostAuthGoogleSdkReq postAuthGoogleSdkReq){
        PostAuthGoogleSdkRes postAuthGoogleSdkRes = userService.googleSdkLogin(postAuthGoogleSdkReq);
        return ResponseEntity.ok(new BaseResponse<>(postAuthGoogleSdkRes));
    }

    // 6. 토큰 재발급
    @GetMapping("/users/auth/reissue")
    public ResponseEntity<BaseResponse<GetReissueRes>> reissue(HttpServletRequest request) throws JsonProcessingException {

        System.out.println("reissue");
        System.out.println(request.getHeader("X-ACCESS-TOKEN"));
        String jwtToken = jwtAuthenticationFilter.resolveToken(request);
        System.out.println(jwtToken);
        String userEmailandProvider = jwtTokenProvider.getUserEmailAndProviderFromJWT(jwtToken);
        System.out.println(userEmailandProvider);
        GetReissueRes getReissueRes = userService.reissue(userEmailandProvider);

        return ResponseEntity.ok(new BaseResponse<>(getReissueRes));
    }
}
