package com.uou.capstone.domain.web.seller.dto;

import com.uou.capstone.global.config.security.jwt.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLoginRes {
    private TokenDto token;
    private Long sellerIdx;
    private String name;
    private int first_login;
    private int menu_register;
}
