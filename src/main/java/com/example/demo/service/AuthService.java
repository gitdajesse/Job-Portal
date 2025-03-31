package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.UserType;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user, String role) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password before saving

        if (role.equalsIgnoreCase("EMPLOYER")) {
            user.setRole(UserType.EMPLOYER); // Use Enum instead of String
        } else {
            user.setRole(UserType.JOBSEEKER); // Use Enum instead of String
        }

        return userRepository.save(user);
    }

    // Method to authenticate user by comparing the password
    public boolean authenticateUser(String email, String password) {
        // Find the user by email from the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("🔑 Provided Password: {}", password.trim());
        log.info("🔑 Stored Hashed Password: {}", user.getPassword());

        // Use passwordEncoder to compare the entered password with the encoded password
        boolean isMatch = passwordEncoder.matches(password, user.getPassword());
        log.info("Password Match: {}", isMatch);

        return isMatch;
    }
}
