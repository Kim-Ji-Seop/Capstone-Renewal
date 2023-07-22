package com.umc.jaetteoli.global.config.error.exception;

import com.umc.jaetteoli.global.config.error.BaseResponseStatus;
import com.umc.jaetteoli.global.config.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final int code;
    private final String errorMessage;

    public BaseException(ErrorCode errorcode) {
        super(errorcode.getErrorMessage());
        this.errorCode = errorcode;
        this.code = errorcode.getCode();
        this.errorMessage = errorcode.getErrorMessage();
    }

    public BaseException(ErrorCode errorcode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorcode;
        this.code = errorcode.getCode();
        this.errorMessage = errorMessage;
    }

    // With Cause Exception
    public BaseException(ErrorCode errorcode, Exception cause) {
        super(errorcode.getErrorMessage(), cause);
        this.errorCode = errorcode;
        this.code = errorcode.getCode();
        this.errorMessage = errorcode.getErrorMessage();
    }

    public BaseException(ErrorCode errorcode, String errorMessage, Exception cause) {
        super(errorMessage, cause);
        this.errorCode = errorcode;
        this.code = errorcode.getCode();
        this.errorMessage = errorMessage;
    }

    public int getCode() {
        return code;
    }
}
