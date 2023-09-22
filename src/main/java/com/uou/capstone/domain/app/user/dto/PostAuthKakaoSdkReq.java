package com.uou.capstone.domain.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostAuthKakaoSdkReq {
    private String profileImg;
    private String email;
    private String nickname;
    private String provider;
}
