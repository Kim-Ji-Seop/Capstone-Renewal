package com.uou.capstone.global.config.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "비회원"),
    CUSTOMER("ROLE_USER", "회원"),
    SELLER("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
