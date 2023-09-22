package com.uou.capstone.domain.app.user;

import com.uou.capstone.domain.app.user.dto.*;
import com.uou.capstone.domain.app.user.service.UserService;
import com.uou.capstone.global.config.error.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class UserController {
    private final UserService userService;
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

}
