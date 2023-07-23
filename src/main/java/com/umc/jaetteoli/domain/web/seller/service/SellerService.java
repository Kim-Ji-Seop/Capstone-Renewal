package com.umc.jaetteoli.domain.web.seller.service;

import com.umc.jaetteoli.domain.web.seller.dto.PostSignUpSellerReq;
import com.umc.jaetteoli.domain.web.seller.dto.PostSignUpSellerRes;
import com.umc.jaetteoli.domain.web.seller.entity.Seller;
import com.umc.jaetteoli.domain.web.seller.repository.SellerRepository;
import com.umc.jaetteoli.global.config.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.umc.jaetteoli.global.config.error.ErrorCode.*;
import static com.umc.jaetteoli.global.util.Regex.*;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = BaseException.class)
    public PostSignUpSellerRes signUp(PostSignUpSellerReq postSignUpSellerReq) throws BaseException {
        // 1. Request값 검사 (빈값여부,정규식 일치 검사)
        if(checkIsEmptySignUpBySeller(postSignUpSellerReq)){ // 빈값여부 check
            throw new BaseException(BAD_REQUEST);
        }
        if(!isRegexUid(postSignUpSellerReq.getUid())){ // 아이디 정규 표현식 예외
            throw new BaseException(INVALID_UID_FORMAT);
        }
        if(!isRegexPassword(postSignUpSellerReq.getPassword())){ // 비밀번호 정규 표현식 예외
            throw new BaseException(INVALID_PW_FORMAT);
        }
        if(!isRegexBirth(postSignUpSellerReq.getBirthday())){ // 생년월일 정규 표현식 예외
            throw new BaseException(INVALID_BIRTH_FORMAT);
        }
        if(!isRegexPhone(postSignUpSellerReq.getPhone())){ // 핸드폰번호 정규 표현식 예외
            throw new BaseException(INVALID_PHONE_NUM_FORMAT);
        }

        // 2. 중복 아이디 검사 및 비밀번호 암호화
        if(sellerRepository.findByUid(postSignUpSellerReq.getUid()).isPresent()){ // 중복 아이디 검사
            throw new BaseException(ID_ALREADY_EXISTS);
        }
        try{ // 비밀번호 암호화 -> 사용자 요청 값 중 비밀번호 최신화
            String encryptPassword = passwordEncoder.encode(postSignUpSellerReq.getPassword());
            postSignUpSellerReq.setPassword(encryptPassword);
        }catch (Exception e){
            throw new BaseException(PASSWORD_ENCRYPTION_FAILURE); // 비밀번호 암호화 실패 시
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
            System.out.println(e);
            throw new BaseException(DATABASE_ERROR);
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
}
