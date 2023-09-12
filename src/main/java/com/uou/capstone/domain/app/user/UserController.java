package com.uou.capstone.domain.app.user;

import com.uou.capstone.domain.app.user.dto.PostAuthEmailBeforeReq;
import com.uou.capstone.domain.app.user.dto.PostAuthEmailBeforeRes;
import com.uou.capstone.domain.app.user.service.UserService;
import com.uou.capstone.global.config.error.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class UserController {
    private final UserService userService;

    @PostMapping("/users/auth/email")
    public ResponseEntity<BaseResponse<PostAuthEmailBeforeRes>> emailCheckBefore(@RequestBody PostAuthEmailBeforeReq postAuthEmailBeforeReq){
        PostAuthEmailBeforeRes postAuthEmailBeforeRes = userService.emailcheckBefore(postAuthEmailBeforeReq);
        return ResponseEntity.ok(new BaseResponse<>(postAuthEmailBeforeRes));
    }

}