package com.uou.capstone.domain.web.seller.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uou.capstone.domain.web.seller.entity.Seller;
import com.uou.capstone.domain.web.seller.repository.SellerRepository;
import com.uou.capstone.global.config.error.exception.BaseException;
import com.uou.capstone.global.config.redis.RedisDao;
import com.uou.capstone.global.config.security.Role;
import com.uou.capstone.global.config.security.jwt.JwtTokenProvider;
import com.uou.capstone.global.config.security.jwt.TokenDto;
import com.uou.capstone.domain.web.seller.dto.*;
import com.uou.capstone.global.config.error.ErrorCode;
import com.uou.capstone.global.util.Regex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {
    private final RedisDao redisDao;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(rollbackFor = BaseException.class)
    public PostSignUpSellerRes signUp(PostSignUpSellerReq postSignUpSellerReq) throws BaseException {
        // 1. Request값 검사 (빈값여부,정규식 일치 검사)
        if(checkIsEmptySignUpBySeller(postSignUpSellerReq)){ // 빈값여부 check
            throw new BaseException(ErrorCode.BAD_REQUEST);
        }
        if(!Regex.isRegexUid(postSignUpSellerReq.getUid())){ // 아이디 정규 표현식 예외
            throw new BaseException(ErrorCode.INVALID_UID_FORMAT);
        }
        if(!Regex.isRegexPassword(postSignUpSellerReq.getPassword())){ // 비밀번호 정규 표현식 예외
            throw new BaseException(ErrorCode.INVALID_PW_FORMAT);
        }
        if(!Regex.isRegexBirth(postSignUpSellerReq.getBirthday())){ // 생년월일 정규 표현식 예외
            throw new BaseException(ErrorCode.INVALID_BIRTH_FORMAT);
        }
        if(!Regex.isRegexPhone(postSignUpSellerReq.getPhone())){ // 핸드폰번호 정규 표현식 예외
            throw new BaseException(ErrorCode.INVALID_PHONE_NUM_FORMAT);
        }

        // 2. 중복 아이디 검사 및 비밀번호 암호화
        if(sellerRepository.findByUid(postSignUpSellerReq.getUid()).isPresent()){ // 중복 아이디 검사
            throw new BaseException(ErrorCode.ID_ALREADY_EXISTS);
        }
        try{ // 비밀번호 암호화 -> 사용자 요청 값 중 비밀번호 최신화
            String encryptPassword = passwordEncoder.encode(postSignUpSellerReq.getPassword());
            postSignUpSellerReq.setPassword(encryptPassword);
        }catch (Exception e){
            throw new BaseException(ErrorCode.PASSWORD_ENCRYPTION_FAILURE); // 비밀번호 암호화 실패 시
        }


        try{
            Seller newSeller = Seller.builder()
                    .name(postSignUpSellerReq.getName())
                    .birthday(postSignUpSellerReq.getBirthday())
                    .phone(postSignUpSellerReq.getPhone())
                    .uid(postSignUpSellerReq.getUid())
                    .password(postSignUpSellerReq.getPassword())
                    .email(postSignUpSellerReq.getEmail())
                    .firstLogin(1)
                    .serviceCheck(postSignUpSellerReq.getServiceCheck())
                    .personalCheck(postSignUpSellerReq.getPersonalCheck())
                    .smsCheck(postSignUpSellerReq.getSmsCheck())
                    .emailCheck(postSignUpSellerReq.getEmailCheck())
                    .callCheck(postSignUpSellerReq.getCallCheck())
                    .role(Role.SELLER)
                    .build();
            // 3. 유저 insert
            newSeller = sellerRepository.save(newSeller);
            // 4. 방금 insert한 유저 반환
            PostSignUpSellerRes checkSeller = PostSignUpSellerRes.builder()
                    .uid(newSeller.getUid())
                    .name(newSeller.getName())
                    .birthday(newSeller.getBirthday())
                    .phone(newSeller.getPhone())
                    .email(newSeller.getEmail())
                    .completeDate(convertTimestampToString(newSeller.getCreatedAt()))
                    .smsCheck(newSeller.getSmsCheck())
                    .emailCheck(newSeller.getEmailCheck())
                    .callCheck(newSeller.getCallCheck())
                    .build();

            return checkSeller;
        }catch (Exception e){
            throw new BaseException(ErrorCode.DATABASE_ERROR);
        }
    }

    /**
     * 입력값 빈값 여부 판별 메소드
     * 이름, 생년월일, 휴대폰 번호, 아이디, 비밀번호 빈값 체크
     */
    public boolean checkIsEmptySignUpBySeller(PostSignUpSellerReq postSignUpSellerReq){
        return postSignUpSellerReq.getUid().length()==0 || postSignUpSellerReq.getPassword().length()==0 || postSignUpSellerReq.getName().length()==0
                || postSignUpSellerReq.getBirthday().length()==0 || postSignUpSellerReq.getEmail().length()==0 || postSignUpSellerReq.getPhone().length()==0;
    }

    public String convertTimestampToString(LocalDateTime localDateTime) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return dateFormat.format(localDateTime);
    }

    @Transactional(rollbackFor = BaseException.class)
    public PostLoginRes login(PostLoginReq postLoginReq) throws BaseException {
        // 1. 빈값확인, 정규식확인
        if(postLoginReq.getUid().length()==0 || postLoginReq.getPassword().length()==0){
            throw new BaseException(ErrorCode.BAD_REQUEST);
        }
        // 2. 회원이 있는지 검색 (uid로 검색)
        Optional<Seller> optionalSeller = sellerRepository.findByUid(postLoginReq.getUid());
        if(!optionalSeller.isPresent()){
            throw new BaseException(ErrorCode.FAILED_TO_LOGIN);
        }
        Seller seller = optionalSeller.get();

        // 3. 입력으로 들어온 password 암호화 및 비교
        if(!passwordEncoder.matches(postLoginReq.getPassword(), seller.getPassword())) {
            throw new BaseException(ErrorCode.FAILED_TO_LOGIN);
        }
        try{
            // 4. jwt 토큰 발급
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(postLoginReq.getUid(), postLoginReq.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            TokenDto token = jwtTokenProvider.generateToken(authentication,seller.getSellerIdx());

            // 5. PostLoginRes 반환
            return PostLoginRes.builder()
                    .token(token)
                    .sellerIdx(seller.getSellerIdx())
                    .name(seller.getName())
                    .first_login(seller.getFirstLogin())
                    .menu_register(seller.getMenuRegister())
                    .build();

        }catch (BadCredentialsException e){
            throw new BaseException(ErrorCode.FAIL_AUTHENTICATION);
        }

    }

    public PostLoginRes reissue(String userUid) throws JsonProcessingException {
        String rtkInRedis = redisDao.getValues(userUid);

        if (Objects.isNull(rtkInRedis)){
            throw new BaseException(ErrorCode.EXPIRED_AUTHENTICATION);
        }

        String rtkUid = jwtTokenProvider.getUserUidFromJWT(rtkInRedis);
        Optional<Seller> seller = sellerRepository.findByUid(userUid);

        Seller authSeller = seller.get();

        Authentication authentication = new UsernamePasswordAuthenticationToken(authSeller, null, authSeller.getAuthorities());

        TokenDto token = jwtTokenProvider.reissueAtk(userUid,rtkUid,seller.get().getSellerIdx(),authentication);

        return PostLoginRes.builder()
                .token(token)
                .sellerIdx(seller.get().getSellerIdx())
                .name(seller.get().getName())
                .first_login(seller.get().getFirstLogin())
                .menu_register(seller.get().getMenuRegister())
                .build();
    }
}
