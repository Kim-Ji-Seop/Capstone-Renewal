package com.umc.jaetteoli.domain.user.dto;

import lombok.Data;

@Data
public class PostSignupReq {
    private String uid;
    private String password;
    private String name;
    private String nickName;
    public PostSignupReq() {}
}
