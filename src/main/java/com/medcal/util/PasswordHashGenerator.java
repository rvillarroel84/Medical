package com.medcal.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("doctor123: " + encoder.encode("doctor123"));
        System.out.println("recepcion123: " + encoder.encode("recepcion123"));
        System.out.println("patient123: " + encoder.encode("patient123"));
    }
}