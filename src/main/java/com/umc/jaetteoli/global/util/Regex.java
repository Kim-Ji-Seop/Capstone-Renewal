package com.umc.jaetteoli.global.util;

import java.util.regex.Pattern;

public class Regex {
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String UID_PATTERN =
            "^[A-Za-z0-9_-]{10,20}$";  // 10~20자의 영문 대소문자, 숫자, 특수문자(_, -)만 사용 가능

    private static final String PASSWORD_PATTERN =
            "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{8,32}$";  // 8~32자의 영문 대소문자, 숫자, 특수문자를 최소 1개 이상 포함

    private static final String NICKNAME_PATTERN =
            "^[a-zA-Z0-9가-힣]{2,10}$";  // 2~10자의 영문 대소문자, 숫자, 한글만 사용 가능

    public static boolean isValidEmail(String email) {
        return Pattern.matches(EMAIL_PATTERN, email);
    }

    public static boolean isValidUid(String uid) {
        return Pattern.matches(UID_PATTERN, uid);
    }

    public static boolean isValidPassword(String password) {
        return Pattern.matches(PASSWORD_PATTERN, password);
    }

    public static boolean isValidNickName(String nickName) {
        return Pattern.matches(NICKNAME_PATTERN, nickName);
    }
}
