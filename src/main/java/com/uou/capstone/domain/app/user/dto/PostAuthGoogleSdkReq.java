package com.uou.capstone.domain.app.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostAuthGoogleSdkReq {
    private String profileImg;
    private String email;
    private String nickname;
    private String provider;
}
