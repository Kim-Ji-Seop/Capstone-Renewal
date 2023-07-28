package com.umc.jaetteoli.domain.web.seller.dto;

import com.umc.jaetteoli.global.config.security.jwt.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLoginRes {
    private TokenDto token;
    private Long sellerIdx;
    private String name;
    private int first_login;
    private int menu_register;
    private String store_name;
    private String store_status;
}
