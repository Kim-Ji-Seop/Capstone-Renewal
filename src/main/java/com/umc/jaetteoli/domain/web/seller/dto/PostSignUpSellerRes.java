package com.umc.jaetteoli.domain.web.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostSignUpSellerRes {
    private String uid;
    private String name;
    private String birthday;
    private String phone;
    private String email;
    private String completeDate; // 처리날짜
    private int smsCheck;
    private int emailCheck;
    private int CallCheck;
}
