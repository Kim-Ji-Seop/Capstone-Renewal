package com.umc.jaetteoli.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.umc.jaetteoli.domain.user.dto.PostSignupReq;
import com.umc.jaetteoli.domain.user.dto.PostSignupRes;
import com.umc.jaetteoli.domain.user.service.UserService;
import com.umc.jaetteoli.global.config.BaseException;
import com.umc.jaetteoli.global.config.BaseResponse;
import com.umc.jaetteoli.global.config.BaseResponseStatus;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<BaseResponse<PostSignupRes>> signUp(@RequestBody PostSignupReq postSignUpReq) {
        try {
            PostSignupRes postSignUpRes = userService.signUp(postSignUpReq);
            return ResponseEntity.ok(new BaseResponse<>(postSignUpRes));
        } catch (BaseException baseException) {
            BaseResponseStatus errorCode = baseException.getStatus();
            return ResponseEntity.status(errorCode.getHttpStatus())
                    .body(new BaseResponse<>(errorCode.getMessage(), errorCode.getCode()));
        }
    }

    @GetMapping("")
    public String helloWorld(){
        return "hello world";
    }

    @GetMapping("/test")
    public String test(){
        return "Test Url";
    }
}
