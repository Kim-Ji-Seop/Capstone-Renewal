package com.umc.jaetteoli.domain.web.seller;

import com.umc.jaetteoli.domain.web.seller.dto.*;
import com.umc.jaetteoli.domain.web.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import com.umc.jaetteoli.global.config.error.exception.BaseException;
import com.umc.jaetteoli.global.config.error.BaseResponse;

import static com.umc.jaetteoli.global.config.error.BaseResponseStatus.BAD_REQUEST;
import static com.umc.jaetteoli.global.config.error.BaseResponseStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web")
public class SellerController {
    // 생성자 주입
    private final SellerService sellerService;

    @GetMapping("/test")
    public ResponseEntity<BaseResponse> userTest(){
        try{
            return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
        } catch(BaseException exception){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new BaseResponse<>(BAD_REQUEST));
        }
    }

    /**
     * 회원가입 api sheet num.1
     * @param postSignUpSellerReq
     * @return BaseResponse<>(postSignUpSellerRes)
     */
    @PostMapping("/jat/sellers")
    public ResponseEntity<BaseResponse<PostSignUpSellerRes>> signUp(@RequestBody PostSignUpSellerReq postSignUpSellerReq) {
        PostSignUpSellerRes postSignUpSellerRes = sellerService.signUp(postSignUpSellerReq);
        return ResponseEntity.ok(new BaseResponse<>(postSignUpSellerRes));
    }


    @PostMapping("/jat/sellers/authy")
    public ResponseEntity<BaseResponse<PostSignUpAuthyRes>> userAuthy(@RequestBody PostSignUpAuthyReq postSignUpAuthyReq) {
        PostSignUpAuthyRes postSignUpAuthyRes = sellerService.userAuthy(postSignUpAuthyReq);
        return ResponseEntity.ok(new BaseResponse<>(postSignUpAuthyRes));
    }

    @PostMapping("/jat/sellers/login")
    public ResponseEntity<BaseResponse<PostLoginRes>> login(@RequestBody PostLoginReq postLoginReq){
        PostLoginRes postLoginRes = sellerService.login(postLoginReq);
        return ResponseEntity.ok(new BaseResponse<>(postLoginRes));
    }
}
