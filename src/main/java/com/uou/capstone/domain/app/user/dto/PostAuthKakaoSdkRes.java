package com.uou.capstone.domain.app.user.dto;

import com.uou.capstone.global.config.security.jwt.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostAuthKakaoSdkRes {
    private TokenDto tokenDto;
    private Long userIdx;
    private String nickname;
}
