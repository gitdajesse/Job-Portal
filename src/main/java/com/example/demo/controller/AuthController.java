package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SeekerProfileDTO;
import com.example.demo.dto.UserDto;
import com.example.demo.model.Employer;
import com.example.demo.model.Seeker;
import com.example.demo.model.User;
import com.example.demo.model.UserType;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.SeekerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import com.example.demo.exception.SeekerNotFoundException;
import com.example.demo.exception.UserNotFoundException;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@Slf4j

public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final SeekerRepository seekerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, UserRepository userRepository,
            EmployerRepository employerRepository, SeekerRepository seekerRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.employerRepository = employerRepository;
        this.seekerRepository = seekerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        log.info("Registration attempt for email: {}", userDto.getEmail());

        try {
            userDto.setEmail(userDto.getEmail().trim());
            userDto.setPassword(userDto.getPassword().trim());

            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                log.warn("Registration failed - email already exists: {}", userDto.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is already in use!"));
            }

            if (!isValidEmail(userDto.getEmail())) {
                log.warn("Invalid email format: {}", userDto.getEmail());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid email format!"));
            }

            try {
                UserType.valueOf(userDto.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role provided: {}", userDto.getRole());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid role. Must be either JOBSEEKER or EMPLOYER"));
            }

            String rawPassword = userDto.getPassword();
            rawPassword = passwordEncoder.encode(rawPassword);

            log.info("Password successfully hashed for user: {}", userDto.getEmail());

            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail((userDto.getEmail()));
            user.setPassword(rawPassword);
            user.setRole(UserType.valueOf(userDto.getRole().toUpperCase()));
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);

            User savedUser = userService.registerUser(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());

            if (user.getRole() == UserType.EMPLOYER) {
                Employer employer = new Employer();
                employer.setUser(savedUser);
                employer.setCompanyName(userDto.getCompanyName());
                employer.setCompanyInfo(userDto.getCompanyInfo());
                employer.setPhone(userDto.getPhone());
                employer.setLocation(userDto.getLocation());
                employerRepository.save(employer);
                log.info("Employer profile created for user ID: {}", savedUser.getId());
            } else {
                Seeker seeker = new Seeker();
                seeker.setUser(savedUser);
                seeker.setFullName(userDto.getFullName());
                seeker.setPhone(userDto.getPhone());
                seeker.setResume(userDto.getResume());
                seeker.setSkills(userDto.getSkills());
                seeker.setExperience(userDto.getExperience());

                seekerRepository.save(seeker);
                log.info("Job seeker profile created for user ID: {}", savedUser.getId());
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "User registered successfully",
                            "role", user.getRole().toString(),
                            "userId", savedUser.getId()));
        } catch (Exception e) {
            log.error("Registration failed for email: {}", userDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            loginRequest.setEmail(loginRequest.getEmail().trim());
            loginRequest.setPassword(loginRequest.getPassword().trim());

            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("Login failed - user not found: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }

            User user = userOptional.get();

            // Check if the account is locked
            if (user.isAccountLocked()) {
                log.warn("Login failed - account is locked for user: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account is locked due to multiple failed login attempts"));
            }

            // Check password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

                // Lock the account after 3 failed attempts
                if (user.getFailedLoginAttempts() >= 3) {
                    user.setAccountLocked(true);
                    user.setLockTime(new Date()); // Set lock time
                    userRepository.save(user);

                    log.warn("Account locked due to multiple failed login attempts: {}", user.getEmail());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Account locked due to multiple failed attempts"));
                }

                userRepository.save(user);
                log.warn("Login failed - invalid password for user: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }

            // 🔹 Reset failed attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockTime(null);
            userRepository.save(user);

            // 🔹 Generate JWT token
            String role = "ROLE_" + user.getRole().name();
            Long seekerId = user.getId(); // Get the seekerId
            String token = jwtUtil.generateToken(user.getEmail(), role, seekerId); // Pass the seekerId

            log.info("Login successful for user: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userType", role,
                    "email", user.getEmail()));

        } catch (Exception e) {
            log.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed. Please try again."));
        }
    }

    @PutMapping("/users/update")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserDto userDto,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.substring(7));
            log.info("Profile update request for user: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", email);
                        return new RuntimeException("User not found");
                    });

            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            userRepository.save(user);

            log.info("Profile updated successfully for user: {}", email);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "email", user.getEmail()));
        } catch (Exception e) {
            log.error("Profile update failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Profile update failed"));
        }
    }

    @PutMapping("/jobseeker/profile")
    @PreAuthorize("hasAuthority('JOBSEEKER')")
    public ResponseEntity<?> updateSeekerProfile(@RequestBody SeekerProfileDTO profileDTO,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.substring(7));
            log.info("Seeker profile update request for user: {}", email);

            // ✅ Find User
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // ✅ Ensure User is a Job Seeker
            if (user.getRole() != UserType.JOBSEEKER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: Only Job Seekers can update their profile"));
            }

            // ✅ Find Seeker Profile
            Seeker seeker = seekerRepository.findByUser(user)
                    .orElseThrow(() -> new SeekerNotFoundException("Seeker profile not found"));

            // ✅ Input Validations
            if (profileDTO.getSkills() != null && profileDTO.getSkills().length() < 10) {
                return ResponseEntity.badRequest().body(Map.of("error", "Skills must be at least 10 characters long"));
            }

            if (profileDTO.getPhone() != null && !profileDTO.getPhone().matches("\\d{10}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone number must be 10 digits"));
            }

            if (profileDTO.getExperience() != null) {
                try {
                    int experience = Integer.parseInt(profileDTO.getExperience());
                    if (experience < 1) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Experience must be at least 1 year"));
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Experience must be a valid number"));
                }
            }

            if (profileDTO.getResume() == null || profileDTO.getResume().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Resume cannot be empty"));
            }

            // ✅ Update Profile Fields
            if (profileDTO.getFullName() != null)
                seeker.setFullName(profileDTO.getFullName());
            if (profileDTO.getPhone() != null)
                seeker.setPhone(profileDTO.getPhone());
            if (profileDTO.getResume() != null)
                seeker.setResume(profileDTO.getResume());
            if (profileDTO.getSkills() != null)
                seeker.setSkills(profileDTO.getSkills());
            if (profileDTO.getExperience() != null)
                seeker.setExperience(profileDTO.getExperience());

            // ✅ Save Updated Profile
            seekerRepository.save(seeker);
            log.info("Seeker profile updated successfully for user: {}", email);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "updatedFields", List.of("fullName", "phone", "resume", "skills", "experience")));
        } catch (UserNotFoundException | SeekerNotFoundException e) {
            log.error("Profile update error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Seeker profile update failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Profile update failed"));
        }
    }
}
