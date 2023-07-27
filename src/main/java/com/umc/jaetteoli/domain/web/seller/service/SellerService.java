package com.umc.jaetteoli.domain.web.seller.service;

import com.umc.jaetteoli.domain.web.seller.dto.*;
import com.umc.jaetteoli.domain.web.seller.entity.Seller;
import com.umc.jaetteoli.domain.web.seller.repository.SellerRepository;
import com.umc.jaetteoli.global.config.error.exception.BaseException;
import com.umc.jaetteoli.global.config.security.Role;
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

    public PostSignUpAuthyRes userAuthy(PostSignUpAuthyReq postSignUpAuthyReq) throws BaseException {
//        try{
//            // 4) 랜덤 인증번호 생성 (번호)
//            Random rand  = new Random();
//            String certificationNum = "";
//            for(int i=0; i<6; i++) {
//                String ran = Integer.toString(rand.nextInt(10));
//                certificationNum+=ran;
//            }
//
//            // 인증 메시지 생성
//            Message message = new Message();
//            message.setFrom("01043753181");
//            message.setTo(signUpAuthy.getPhoneNum());
//            message.setText("회원가입 본인인증 확인입니다.\n["+certificationNum+"]");
//
//            // coolSMS API 사용하여 사용자 핸드폰에 전송
//            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
//            log.info("coolSMS API요청 :{}", response);
//
//            // DB에 전송 인증정보 저장
//            int smsSendRes = smsDao.smsAuthy(signUpAuthy, certificationNum, "S");
//            return new PostSignUpAuthyRes(smsSendRes);
//
//        }catch(Exception exception){
//            throw new BaseException(COOLSMS_API_ERROR); //  5010 : SMS 인증번호 발송을 실패하였습니다.
//        }
        return null;
    }
}
