package com.uou.capstone.global.config.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class BaseResponse<T> {//BaseResponse 객체를 사용할때 성공, 실패 경우
    private final String message;
    private final int code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 성공
    public BaseResponse(T result) {
        this.message = ErrorCode.SUCCESS.getErrorMessage();
        this.code = ErrorCode.SUCCESS.getCode();
        this.result = result;
    }

    public BaseResponse(ErrorCode errorCode) {
        this.message = errorCode.getErrorMessage();
        this.code= errorCode.getCode();
    }

    public BaseResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }
}
