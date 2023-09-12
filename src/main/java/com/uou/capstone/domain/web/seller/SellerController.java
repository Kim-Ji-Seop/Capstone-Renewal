package com.uou.capstone.domain.web.seller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uou.capstone.domain.web.seller.service.SellerService;
import com.uou.capstone.global.config.security.jwt.JwtAuthenticationFilter;
import com.uou.capstone.global.config.security.jwt.JwtTokenProvider;
import com.uou.capstone.domain.web.seller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uou.capstone.global.config.error.exception.BaseException;
import com.uou.capstone.global.config.error.BaseResponse;

import javax.servlet.http.HttpServletRequest;

import static com.uou.capstone.global.config.error.BaseResponseStatus.BAD_REQUEST;
import static com.uou.capstone.global.config.error.BaseResponseStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web/jat/sellers")
public class SellerController {
    // 생성자 주입
    private final SellerService sellerService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtTokenProvider jwtTokenProvider;
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
    @PostMapping("")
    public ResponseEntity<BaseResponse<PostSignUpSellerRes>> signUp(@RequestBody PostSignUpSellerReq postSignUpSellerReq) {
        PostSignUpSellerRes postSignUpSellerRes = sellerService.signUp(postSignUpSellerReq);
        return ResponseEntity.ok(new BaseResponse<>(postSignUpSellerRes));
    }


    @PostMapping("/login")
    public ResponseEntity<BaseResponse<PostLoginRes>> login(@RequestBody PostLoginReq postLoginReq){
        PostLoginRes postLoginRes = sellerService.login(postLoginReq);
        return ResponseEntity.ok(new BaseResponse<>(postLoginRes));
    }

    @GetMapping("/reissue")
    public ResponseEntity<BaseResponse<PostLoginRes>> reissue(HttpServletRequest request) throws JsonProcessingException {
        System.out.println(request.getHeader("X-ACCESS-TOKEN"));
        String jwtToken=jwtAuthenticationFilter.resolveToken(request);
        String userUid=jwtTokenProvider.getUserUidFromJWT(jwtToken);
        PostLoginRes postLoginRes=sellerService.reissue(userUid);

        return ResponseEntity.ok(new BaseResponse<>(postLoginRes));
    }
}
