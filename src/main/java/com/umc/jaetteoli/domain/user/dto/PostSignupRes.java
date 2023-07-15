package com.umc.jaetteoli.domain.user.dto;

import lombok.Data;

@Data
public class PostSignupRes {
    private Long id;
    private String success="성공";
    public PostSignupRes(Long id) {
        this.id = id;
    }
}
