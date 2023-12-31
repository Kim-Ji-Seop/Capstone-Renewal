package com.uou.capstone.global.config.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

@Getter
public enum ErrorCode {
    // 고정 코드
    SUCCESS(HttpStatus.OK,200,  "요청에 성공하였습니다."),
    BAD_REQUEST( HttpStatus.BAD_REQUEST,400,  "입력값을 확인해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN,  403,"권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, 404,"대상을 찾을 수 없습니다."),

    // 커스텀 코드
    // 회원가입 - 정규표현식, 중복, 빈값
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST,4000,"이메일 정규 표현식 예외입니다."),
    INVALID_PW_FORMAT(HttpStatus.BAD_REQUEST,4001,"비밀번호 정규 표현식 예외입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,4002,"이미 존재하는 이메일입니다"),
    // [User] 로그인
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED,4003,"해당하는 유저를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,4004,"비밀번호가 틀렸습니다"),


    TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, 416, "JWT Token이 존재하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,  417,"유효하지 않은 JWT Token 입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,  418,"만료된 Access Token 입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,  419,"만료된 Refresh Token 입니다."),
    FAIL_AUTHENTICATION(HttpStatus.UNAUTHORIZED,  420,"사용자 인증에 실패하였습니다."),

    EXPIRED_AUTHENTICATION(HttpStatus.UNAUTHORIZED,421,"인증정보가 만료되었습니다."),

    INVALID_KAKAO_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,403,"유효하지 않은 Kakao Access Token입니다."),
    INVALID_HTTP_METHOD(METHOD_NOT_ALLOWED, 3001, "잘못된 Http Method 요청입니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, 3002, "잘못된 입력값입니다."),
    SERVER_INTERNAL_ERROR(INTERNAL_SERVER_ERROR, 3003, "서버 내부에 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,403,"인가되지 않은 사용자입니다."),
    // 이메일 회원가입
    COOLSMS_API_ERROR(INTERNAL_SERVER_ERROR, 501, "SMS 인증번호 발송을 실패하였습니다."),
    EMAIL_SEND_FAILED(INTERNAL_SERVER_ERROR,510,"이메일 전송 실패"),

    PASSWORD_ENCRYPTION_FAILURE(INTERNAL_SERVER_ERROR,5000,"비밀번호 암호화에 실패했습니다."),

    // Database 예외
    DATABASE_ERROR(INTERNAL_SERVER_ERROR,5100,"데이터베이스 오류입니다."),
    REDIS_ERROR(INTERNAL_SERVER_ERROR, 512, "Redis 연결에 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String errorMessage;

    ErrorCode( HttpStatus httpStatus, int code, String errorMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
