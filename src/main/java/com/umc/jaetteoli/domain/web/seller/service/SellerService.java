package com.umc.jaetteoli.domain.web.seller.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.umc.jaetteoli.domain.web.seller.dto.*;
import com.umc.jaetteoli.domain.web.seller.entity.Seller;
import com.umc.jaetteoli.domain.web.seller.repository.SellerRepository;
import com.umc.jaetteoli.domain.web.sms.entity.Sms;
import com.umc.jaetteoli.domain.web.sms.repository.SmsRepository;
import com.umc.jaetteoli.global.config.error.exception.BaseException;
import com.umc.jaetteoli.global.config.redis.RedisDao;
import com.umc.jaetteoli.global.config.security.Role;
import com.umc.jaetteoli.global.config.security.jwt.JwtTokenProvider;
import com.umc.jaetteoli.global.config.security.jwt.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static com.umc.jaetteoli.global.config.error.ErrorCode.*;
import static com.umc.jaetteoli.global.util.Regex.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {
    @Value("${sms.apiKey}")
    private String apiKey;
    @Value("${sms.secret}")
    private String secret;
    private final RedisDao redisDao;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private DefaultMessageService messageService;
    private final SmsRepository smsRepository;

    /**
     * @PostConstruct 애너테이션이 붙은 메서드는 Spring이 Bean을 초기화한 후,
     * 즉 모든 의존성 주입이 완료된 후에 호출된다.
     * 이 방법을 사용하면 apiKey와 secret 값이 설정된 후에 NurigoApp.INSTANCE.initialize를 호출할 수 있다.
     */
    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey,secret,"https://api.coolsms.co.kr");
    }
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

    @Transactional(rollbackFor = BaseException.class)
    public PostSignUpAuthyRes userAuthy(PostSignUpAuthyReq postSignUpAuthyReq) throws BaseException {
        try{
            // 랜덤 인증번호 생성 (번호)
            Random rand  = new Random();
            String certificationNum = "";
            for(int i=0; i<6; i++) {
                String ran = Integer.toString(rand.nextInt(10));
                certificationNum+=ran;
            }

            // 인증 메시지 생성
            Message message = new Message();
            message.setFrom("01043753181");
            message.setTo(postSignUpAuthyReq.getPhoneNum());
            message.setText("회원가입 본인인증 확인입니다.\n["+certificationNum+"]");

            // coolSMS API 사용하여 사용자 핸드폰에 전송
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("coolSMS API요청 :{}", response);

        }catch(Exception exception){
            throw new BaseException(COOLSMS_API_ERROR); //  5010 : SMS 인증번호 발송을 실패하였습니다.
        }
            // DB에 전송 인증정보 저장
        try{
            Sms newSms = Sms.builder()
                    .phone(postSignUpAuthyReq.getPhoneNum())
                    .name(postSignUpAuthyReq.getName())
                    .uid(postSignUpAuthyReq.getBirth())
                    .certificationNum(postSignUpAuthyReq.getCertificationNum())
                    .created(LocalDateTime.now())
                    .updated(LocalDateTime.now())
                    .status(Sms.Status.SIGN_UP)
                    .build();
            // 3. 유저 insert
            newSms = smsRepository.save(newSms);
            // 4. 방금 insert한 유저 반환
            return new PostSignUpAuthyRes(Math.toIntExact(newSms.getSmsIdx()));
        }catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }



    @Transactional(rollbackFor = BaseException.class)
    public PostLoginRes login(PostLoginReq postLoginReq) throws BaseException {
        // 1. 빈값확인, 정규식확인
        if(postLoginReq.getUid().length()==0 || postLoginReq.getPassword().length()==0){
            throw new BaseException(BAD_REQUEST);
        }
        // 2. 회원이 있는지 검색 (uid로 검색)
        Optional<Seller> optionalSeller = sellerRepository.findByUid(postLoginReq.getUid());
        //System.out.println(optionalSeller.get().getUsername()+" / "+optionalSeller.get().getPassword());
        if(!optionalSeller.isPresent()){
            throw new BaseException(FAILED_TO_LOGIN);
        }
        Seller seller = optionalSeller.get();

        // 3. 입력으로 들어온 password 암호화 및 비교
        if(!passwordEncoder.matches(postLoginReq.getPassword(), seller.getPassword())) {
            throw new BaseException(FAILED_TO_LOGIN);
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
            throw new BaseException(FAIL_AUTHENTICATION);
        }

    }

    public PostLoginRes reissue(String userUid) throws JsonProcessingException {
        String rtkInRedis = redisDao.getValues(userUid);

        if (Objects.isNull(rtkInRedis)){
            throw new BaseException(EXPIRED_AUTHENTICATION);
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
