package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.UserType;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Register a new user
    public User registerUser(User user) {
        // Ensure the role is valid
        if (user.getRole() != UserType.JOBSEEKER && user.getRole() != UserType.EMPLOYER) {
            throw new IllegalArgumentException("Invalid role. Must be either JOBSEEKER or EMPLOYER");
        }

        // Ensure password is hashed
        if (!user.getPassword().startsWith("$2a$")) { // BCrypt hashes start with "$2a$"
            throw new IllegalArgumentException("Password must be hashed before saving!");
        }

        log.info("Saving user with hashed password: {}", user.getEmail());

        return userRepository.save(user);
    }

    // Other user-related business logic can go here, such as updating or finding
    // users.
}
