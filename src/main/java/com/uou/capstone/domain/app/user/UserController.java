package com.uou.capstone.domain.app.user;

import com.uou.capstone.domain.app.user.dto.PostAuthEmailReq;
import com.uou.capstone.domain.app.user.dto.PostAuthEmailRes;
import com.uou.capstone.domain.app.user.dto.PostSignUpUserReq;
import com.uou.capstone.domain.app.user.dto.PostSignUpUserRes;
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
    // 이메일 인증코드 전송 및 인증
    @PostMapping("/users/auth/email")
    public ResponseEntity<BaseResponse<PostAuthEmailRes>> emailCheck(@RequestBody PostAuthEmailReq postAuthEmailReq){
        PostAuthEmailRes postAuthEmailRes = userService.emailcheck(postAuthEmailReq);
        return ResponseEntity.ok(new BaseResponse<>(postAuthEmailRes));
    }

    @PostMapping("/users/auth")
    public ResponseEntity<BaseResponse<PostSignUpUserRes>> emailSignUp(@RequestBody PostSignUpUserReq postSignUpUserReq){
        PostSignUpUserRes postSignUpUserRes = userService.emailSignUp(postSignUpUserReq);
        return ResponseEntity.ok(new BaseResponse<>(postSignUpUserRes));
    }

}
