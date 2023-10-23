package com.uou.capstone.domain.app.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uou.capstone.domain.app.user.dto.*;
import com.uou.capstone.domain.app.user.entity.User;
import com.uou.capstone.domain.app.user.repository.UserRepository;
import com.uou.capstone.global.config.EmailService;
import com.uou.capstone.global.config.error.exception.BaseException;
import com.uou.capstone.global.config.redis.RedisDao;
import com.uou.capstone.global.config.security.Role;
import com.uou.capstone.global.config.security.jwt.JwtTokenProvider;
import com.uou.capstone.global.config.security.jwt.TokenDto;
import com.uou.capstone.global.config.security.userdetails.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static com.uou.capstone.global.config.error.ErrorCode.*;
import static com.uou.capstone.global.util.Regex.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements EmailService {
    private final JavaMailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final CustomUserDetailService customUserDetailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;
    public PostAuthEmailRes emailcheck(PostAuthEmailReq postAuthEmailReq) throws BaseException {
        String ePw = createKey();
        String verificationCode;
        try{
            verificationCode = sendSimpleMessage(postAuthEmailReq.getEmail(),ePw); // 이메일 전송
        }catch (Exception e){
            throw new BaseException(EMAIL_SEND_FAILED);
        }
        return new PostAuthEmailRes(verificationCode);
    }


    private MimeMessage createMessage(String to, String ePw)throws Exception{
        System.out.println("보내는 대상 : "+ to);
        System.out.println("인증 번호 : "+ePw);
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);//보내는 대상
        message.setSubject("이메일 인증 테스트");//제목

        String msgg="";
        msgg+= "<div style='margin:20px;'>";
        msgg+= "<h1> 안녕하세요 김지섭입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>아래 코드를 복사해 입력해주세요<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다.<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= ePw+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("이메일","kimjiseop"));//보내는 사람

        return message;
    }

    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return key.toString();
    }
    @Override
    public String sendSimpleMessage(String to,String ePw)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = createMessage(to,ePw);
        try{//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return ePw;
    }
    @Transactional(rollbackFor = BaseException.class)
    public PostSignUpUserRes emailSignUp(PostSignUpUserReq postSignUpUserReq) throws BaseException {
        if(checkIsEmptySignUpByEmail(postSignUpUserReq)){
            throw new BaseException(BAD_REQUEST);
        }
        if(!isRegexEmail(postSignUpUserReq.getEmail())){
            throw new BaseException(INVALID_EMAIL_FORMAT);
        }
        if(!isRegexPassword(postSignUpUserReq.getPassword())){
            throw new BaseException(INVALID_PW_FORMAT);
        }
        if(userRepository.findByEmail(postSignUpUserReq.getEmail()).isPresent()){
            throw new BaseException(EMAIL_ALREADY_EXISTS);
        }
        try{ // 비밀번호 암호화 -> 사용자 요청 값 중 비밀번호 최신화
            String encryptPassword = passwordEncoder.encode(postSignUpUserReq.getPassword());
            postSignUpUserReq.setPassword(encryptPassword);
        }catch (Exception e){
            throw new BaseException(PASSWORD_ENCRYPTION_FAILURE); // 비밀번호 암호화 실패 시
        }
        try{
            User newUser = User.builder()
                    .email(postSignUpUserReq.getEmail())
                    .password(postSignUpUserReq.getPassword())
                    .name(postSignUpUserReq.getName())
                    .nickname(postSignUpUserReq.getNickname())
                    .role(Role.CUSTOMER)
                    .provider(User.Provider.EMAIL)
                    .build();

            newUser = userRepository.save(newUser);
            PostSignUpUserRes postSignUpUserRes = new PostSignUpUserRes(newUser.getUserIdx());
            return postSignUpUserRes;
        }catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public boolean checkIsEmptySignUpByEmail(PostSignUpUserReq postSignUpUserReq){
        return postSignUpUserReq.getEmail().isEmpty() || postSignUpUserReq.getPassword().isEmpty()
                || postSignUpUserReq.getName().isEmpty() || postSignUpUserReq.getNickname().isEmpty();
    }
    public PostLoginEmailRes emailLogin(PostLoginEmailReq postLoginEmailReq) throws BaseException {
        if(checkIsEmptyLoginByEmail(postLoginEmailReq)){
            throw new BaseException(BAD_REQUEST);
        }
        if(!isRegexEmail(postLoginEmailReq.getEmail())){
            throw new BaseException(INVALID_EMAIL_FORMAT);
        }
        if(!isRegexPassword(postLoginEmailReq.getPassword())){
            throw new BaseException(INVALID_PW_FORMAT);
        }
        try {
            // 1. 이메일:EMAIL 합쳐서 유저판별
            String combinedEmailAndProvider = postLoginEmailReq.getEmail() + ":EMAIL";
            UserDetails userDetails = customUserDetailService.loadUserByUsername(combinedEmailAndProvider);

            // 2. 패스워드 검증
            User user = (User) userDetails;
            if (!checkPassword(user, postLoginEmailReq.getPassword())) {
                throw new BaseException(INVALID_PASSWORD);
            }

            // 3. jwt 토큰 발급
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(combinedEmailAndProvider, postLoginEmailReq.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            TokenDto token = jwtTokenProvider.generateToken(authentication,user.getUserIdx());

            // 4. PostLoginRes 반환
            return PostLoginEmailRes.builder()
                    .tokenDto(token)
                    .userIdx(user.getUserIdx())
                    .nickname(user.getNickname())
                    .build();
        } catch (UsernameNotFoundException e) {
            throw new BaseException(USER_NOT_FOUND);
        } catch (BadCredentialsException e){
            throw new BaseException(FAIL_AUTHENTICATION);
        } catch (Exception e) {
            throw new BaseException(NOT_FOUND);
        }
    }
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public boolean checkIsEmptyLoginByEmail(PostLoginEmailReq postLoginEmailReq){
        return postLoginEmailReq.getEmail().isEmpty() || postLoginEmailReq.getPassword().isEmpty();
    }
    public PostAuthKakaoSdkRes kakaoSdkLogin(PostAuthKakaoSdkReq postAuthKakaoSdkReq) throws BaseException {
        User user;
        Optional<User> existingUser = userRepository.findByEmailAndProvider(postAuthKakaoSdkReq.getEmail(), User.Provider.KAKAO);

        // 사용자가 데이터베이스에 이미 존재하는 경우
        if(existingUser.isPresent()) {
            user = existingUser.get();
        }
        // 최초 로그인인 경우
        else {
            // 카카오 로그인 유저 임시 비밀번호 만든다
            String encryptPassword = passwordEncoder.encode(postAuthKakaoSdkReq.getEmail());
            try {
                user = User.builder()
                        .email(postAuthKakaoSdkReq.getEmail())
                        .password(encryptPassword)
                        .name(postAuthKakaoSdkReq.getNickname())
                        .nickname(postAuthKakaoSdkReq.getNickname())
                        .role(Role.CUSTOMER)
                        .profileUrl(postAuthKakaoSdkReq.getProfileImg())
                        .provider(User.Provider.KAKAO)
                        .build();
                // 디비에 저장
                user = userRepository.save(user);
            } catch (BaseException e) {
                throw new BaseException(DATABASE_ERROR);
            }
        }

        try {
            // jwt 토큰 발급
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));
            UserDetails userDetails = user;
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            TokenDto token = jwtTokenProvider.generateToken(authentication, user.getUserIdx());

            // PostAuthKakaoSdkRes 반환
            return PostAuthKakaoSdkRes.builder()
                    .tokenDto(token)
                    .userIdx(user.getUserIdx())
                    .nickname(user.getNickname())
                    .build();

        } catch (BadCredentialsException e) {
            throw new BaseException(FAIL_AUTHENTICATION);
        }
    }


    public PostAuthGoogleSdkRes googleSdkLogin(PostAuthGoogleSdkReq postAuthGoogleSdkReq) throws BaseException {
        User user;
        Optional<User> existingUser = userRepository.findByEmailAndProvider(postAuthGoogleSdkReq.getEmail(), User.Provider.GOOGLE);

        // 사용자가 데이터베이스에 이미 존재하는 경우
        if(existingUser.isPresent()) {
            user = existingUser.get();
        }
        // 최초 로그인인 경우
        else {
            // 카카오 로그인 유저 임시 비밀번호 만든다
            String encryptPassword = passwordEncoder.encode(postAuthGoogleSdkReq.getEmail());
            try {
                user = User.builder()
                        .email(postAuthGoogleSdkReq.getEmail())
                        .password(encryptPassword)
                        .name(postAuthGoogleSdkReq.getNickname())
                        .nickname(postAuthGoogleSdkReq.getNickname())
                        .role(Role.CUSTOMER)
                        .profileUrl(postAuthGoogleSdkReq.getProfileImg())
                        .provider(User.Provider.GOOGLE)
                        .build();
                // 디비에 저장
                user = userRepository.save(user);
            } catch (BaseException e) {
                throw new BaseException(DATABASE_ERROR);
            }
        }

        try {
            // jwt 토큰 발급
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));
            UserDetails userDetails = user;
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            TokenDto token = jwtTokenProvider.generateToken(authentication, user.getUserIdx());

            // PostAuthKakaoSdkRes 반환
            return PostAuthGoogleSdkRes.builder()
                    .tokenDto(token)
                    .userIdx(user.getUserIdx())
                    .nickname(user.getNickname())
                    .build();

        } catch (BadCredentialsException e) {
            throw new BaseException(FAIL_AUTHENTICATION);
        }
    }

    public GetReissueRes reissueAtk(String userEmailandProvider) throws BaseException, JsonProcessingException {
        String rtkInRedis = redisDao.getValues(userEmailandProvider); // 리프레쉬 토큰 가져오기

        if (Objects.isNull(rtkInRedis)){
            throw new BaseException(EXPIRED_AUTHENTICATION);
        }

        String rtkKey = jwtTokenProvider.getUserEmailAndProviderFromJWT(rtkInRedis); // 키값 가져오기
        UserDetails userDetails = customUserDetailService.loadUserByUsername(userEmailandProvider);

        User user = (User) userDetails;

        try{
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            TokenDto token = jwtTokenProvider.reissueAtk(userEmailandProvider, rtkKey, user.getUserIdx(), authentication);

            return GetReissueRes.builder()
                    .tokenDto(token)
                    .userIdx(user.getUserIdx())
                    .nickname(user.getNickname())
                    .provider(user.getProvider().toString())
                    .build();
        }catch (BadCredentialsException e){
            throw new BaseException(FAIL_AUTHENTICATION);
        }
    }


}
