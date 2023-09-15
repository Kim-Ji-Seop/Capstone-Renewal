package com.uou.capstone.domain.app.user.service;

import com.uou.capstone.domain.app.user.dto.PostAuthEmailReq;
import com.uou.capstone.domain.app.user.dto.PostAuthEmailRes;
import com.uou.capstone.domain.app.user.dto.PostSignUpUserReq;
import com.uou.capstone.domain.app.user.dto.PostSignUpUserRes;
import com.uou.capstone.domain.app.user.entity.User;
import com.uou.capstone.domain.app.user.repository.UserRepository;
import com.uou.capstone.global.config.EmailService;
import com.uou.capstone.global.config.error.exception.BaseException;
import com.uou.capstone.global.config.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
                    .oAuthType(User.OAuthType.EMAIL)
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
}
