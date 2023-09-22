package com.uou.capstone.domain.app.user.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.uou.capstone.domain.app.user.dto.*;
import com.uou.capstone.domain.app.user.entity.User;
import com.uou.capstone.domain.app.user.repository.UserRepository;
import com.uou.capstone.global.config.EmailService;
import com.uou.capstone.global.config.error.exception.BaseException;
import com.uou.capstone.global.config.security.Role;
import com.uou.capstone.global.config.security.jwt.JwtTokenProvider;
import com.uou.capstone.global.config.security.jwt.TokenDto;
import com.uou.capstone.global.util.SHA256;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
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
    private final JwtTokenProvider jwtTokenProvider;
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

    public PostAuthKakaoSdkRes kakaoSdkLogin(PostAuthKakaoSdkReq postAuthKakaoSdkReq) throws BaseException {
        User user;
        // 1. 카카오 로그인 유저 임시 비밀번호 만든다
        String encryptPassword = passwordEncoder.encode(postAuthKakaoSdkReq.getEmail());
        try{
            user = User.builder()
                    .email(postAuthKakaoSdkReq.getEmail())
                    .password(encryptPassword)
                    .name(postAuthKakaoSdkReq.getNickname())
                    .nickname(postAuthKakaoSdkReq.getNickname())
                    .role(Role.CUSTOMER)
                    .profileUrl(postAuthKakaoSdkReq.getProfileImg())
                    .provider(User.Provider.KAKAO)
                    .build();
            // 2. 디비에 저장
            user = userRepository.save(user);
            // 3. BaseException말고도 다른 Exception도 처리해야함. -> 추후 변경
        }catch (BaseException e){
            throw new BaseException(DATABASE_ERROR);
        }
        try{
            // 4. jwt 토큰 발급
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString()));
            UserDetails userDetails = user;  // User 엔터티 인스턴스를 사용합니다.
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            TokenDto token = jwtTokenProvider.generateToken(authentication, user.getUserIdx(),user.getProvider());

            // 5. PostAuthKakaoSdkRes 반환
            return PostAuthKakaoSdkRes.builder()
                    .tokenDto(token)
                    .userIdx(user.getUserIdx())
                    .nickname(user.getNickname())
                    .build();

        }catch (BadCredentialsException e){
            throw new BaseException(FAIL_AUTHENTICATION);
        }
    }
}
