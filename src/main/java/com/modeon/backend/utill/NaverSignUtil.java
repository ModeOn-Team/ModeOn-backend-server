package com.modeon.backend.utill;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class NaverSignUtil {

    public static String generateSignature(String clientId, String clientSecret, Long timestamp) {
        String password = clientId + "_" + timestamp;
        String hashedPw = BCrypt.hashpw(password, clientSecret);
        return Base64.getUrlEncoder().encodeToString(hashedPw.getBytes(StandardCharsets.UTF_8));
    }
}
