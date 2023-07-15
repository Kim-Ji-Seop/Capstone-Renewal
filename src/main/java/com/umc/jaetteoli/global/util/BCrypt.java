package com.umc.jaetteoli.global.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCrypt {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encrypt(String text) {
        return encoder.encode(text);
    }

    public static boolean matches(String text, String encodedText) {
        return encoder.matches(text, encodedText);
    }
}
