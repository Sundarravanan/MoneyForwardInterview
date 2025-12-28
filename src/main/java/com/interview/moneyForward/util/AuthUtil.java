package com.interview.moneyForward.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;

public class AuthUtil {

    public static String[] extractCredentials(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) return null;

        try {
            String decoded = new String(
                    Base64.getDecoder().decode(auth.substring(6)),
                    StandardCharsets.UTF_8
            );
            return decoded.split(":", 2);
        } catch (Exception e) {
            return null;
        }
    }
}