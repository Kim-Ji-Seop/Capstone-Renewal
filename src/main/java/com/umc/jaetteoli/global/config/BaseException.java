package com.umc.jaetteoli.global.config;

public class BaseException extends RuntimeException {
    private final BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        this.status = status;
    }

    public BaseResponseStatus getStatus() {
        return this.status;
    }
}
