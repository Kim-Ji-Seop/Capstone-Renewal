package com.umc.jaetteoli.domain.web.seller;

import com.umc.jaetteoli.domain.web.seller.dto.PostSignUpSellerReq;
import com.umc.jaetteoli.domain.web.seller.dto.PostSignUpSellerRes;
import com.umc.jaetteoli.domain.web.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/jat/sellers")
    public ResponseEntity<BaseResponse<PostSignUpSellerRes>> signUp(@RequestBody PostSignUpSellerReq postSignUpSellerReq) {
        PostSignUpSellerRes postSignUpSellerRes = sellerService.signUp(postSignUpSellerReq);
        return ResponseEntity.ok(new BaseResponse<>(postSignUpSellerRes));
    }
}
