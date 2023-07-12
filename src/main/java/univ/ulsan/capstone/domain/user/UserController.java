package univ.ulsan.capstone.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import univ.ulsan.capstone.domain.user.dto.PostSignupReq;
import univ.ulsan.capstone.domain.user.dto.PostSignupRes;
import univ.ulsan.capstone.domain.user.service.UserService;
import univ.ulsan.capstone.global.config.BaseException;
import univ.ulsan.capstone.global.config.BaseResponse;
import univ.ulsan.capstone.global.config.BaseResponseStatus;

@RestController
@RequiredArgsConstructor
public class UserController {
    @Value("${spring.profiles.active:}")
    private String activeProfile;

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

    @GetMapping("/profile")
    public String profile(){
        return activeProfile;
    }
}
