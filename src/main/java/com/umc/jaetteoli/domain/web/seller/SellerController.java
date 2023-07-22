package com.umc.jaetteoli.domain.web.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.umc.jaetteoli.global.config.BaseException;
import com.umc.jaetteoli.global.config.BaseResponse;

import static com.umc.jaetteoli.global.config.BaseResponseStatus.BAD_REQUEST;
import static com.umc.jaetteoli.global.config.BaseResponseStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web")
public class SellerController {
    @GetMapping("/test")

    public ResponseEntity<BaseResponse> userTest(){
        try{
            return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
        } catch(BaseException exception){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new BaseResponse<>(BAD_REQUEST));
        }
    }

}
