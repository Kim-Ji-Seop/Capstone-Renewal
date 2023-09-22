package com.uou.capstone.domain.app.user.dto;

import com.uou.capstone.global.config.security.jwt.TokenDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostAuthGoogleSdkRes {
    private TokenDto tokenDto;
    private Long userIdx;
    private String nickname;
}
