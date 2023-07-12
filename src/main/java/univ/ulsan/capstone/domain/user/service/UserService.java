package univ.ulsan.capstone.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univ.ulsan.capstone.domain.user.dto.PostSignupReq;
import univ.ulsan.capstone.domain.user.dto.PostSignupRes;
import univ.ulsan.capstone.domain.user.entity.UserEntity;
import univ.ulsan.capstone.domain.user.repository.UserRepository;
import univ.ulsan.capstone.global.config.BaseException;
import univ.ulsan.capstone.global.util.BCrypt;

import static univ.ulsan.capstone.global.config.BaseResponseStatus.BAD_REQUEST;
import static univ.ulsan.capstone.global.config.BaseResponseStatus.INVALID_VALUE;
import static univ.ulsan.capstone.global.util.Regex.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public PostSignupRes signUp(PostSignupReq postSignUpReq) throws BaseException {
        // 아이디, 비밀번호, 닉네임 정규식 처리
        if(postSignUpReq.getUid().length() == 0 || postSignUpReq.getPassword().length() == 0 || postSignUpReq.getNickName().length() == 0 || postSignUpReq.getName().length() == 0){
            throw new BaseException(BAD_REQUEST);
        }
        if(!isValidUid(postSignUpReq.getUid())){
            throw new BaseException(INVALID_VALUE);
        }
        if(!isValidPassword(postSignUpReq.getPassword())){
            throw new BaseException(INVALID_VALUE);
        }
        if(!isValidNickName(postSignUpReq.getNickName())){
            throw new BaseException(INVALID_VALUE);
        }

        // 중복 아이디 체크
        if(userRepository.existsByUid(postSignUpReq.getUid())){
            throw new BaseException(INVALID_VALUE);
        }

        // 중복 닉네임 체크
        if(userRepository.existsByNickName(postSignUpReq.getNickName())){
            throw new BaseException(INVALID_VALUE);
        }

        // 비밀번호 암호화
        String pwd = BCrypt.encrypt(postSignUpReq.getPassword());

        UserEntity newUser = UserEntity.builder()
                .uid(postSignUpReq.getUid())
                .password(pwd)
                .name(postSignUpReq.getName())
                .nickName(postSignUpReq.getNickName())
                .build();
        System.out.println(newUser.toString());
        userRepository.save(newUser);

        return new PostSignupRes(newUser.getId());
    }
}
