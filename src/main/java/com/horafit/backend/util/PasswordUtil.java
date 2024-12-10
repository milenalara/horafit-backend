package com.horafit.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    public static String encoder(String psw){
        BCryptPasswordEncoder encrypted = new BCryptPasswordEncoder();
        return encrypted.encode(psw);
    }

}
