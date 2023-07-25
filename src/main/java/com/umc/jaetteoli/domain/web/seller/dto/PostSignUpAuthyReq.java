package com.umc.jaetteoli.domain.web.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostSignUpAuthyReq {
    private String name;
    private String birth;
    private String phoneNum;
    @Nullable
    private String certificationNum;
}
