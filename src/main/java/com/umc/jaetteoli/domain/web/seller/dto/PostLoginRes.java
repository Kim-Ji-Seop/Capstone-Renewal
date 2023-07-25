package com.umc.jaetteoli.domain.web.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLoginRes {
    private String jwt;
    private int sellerIdx;
    private String name;
    private int first_login;
    private int menu_register;
    private String store_name;
    private String store_status;
}
