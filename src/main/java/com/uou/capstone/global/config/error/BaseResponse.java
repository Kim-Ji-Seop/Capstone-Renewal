package com.uou.capstone.global.config.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class BaseResponse<T> {//BaseResponse 객체를 사용할때 성공, 실패 경우
    private final HttpStatus httpStatus;
    private final String message;
    private final int code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 성공
    public BaseResponse(T result) {
        this.httpStatus = ErrorCode.SUCCESS.getHttpStatus();
        this.message = ErrorCode.SUCCESS.getErrorMessage();
        this.code = ErrorCode.SUCCESS.getCode();
        this.result = result;
    }

    public BaseResponse(ErrorCode errorCode) {
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getErrorMessage();
        this.code= errorCode.getCode();
    }

    public BaseResponse(HttpStatus httpStatus, String message, int code) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.code = code;
    }
}
