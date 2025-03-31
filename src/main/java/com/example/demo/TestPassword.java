package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "Test1234";
        String hashedPassword = "$2a$10$pVFk8BlcmoNtSbgSAf2bw.dTmUqSCSQcOyXgSIYG3GIcnrFdbVIhi"; // Your stored password

        System.out.println("Does it match? " + encoder.matches(rawPassword, hashedPassword));
    }
}
