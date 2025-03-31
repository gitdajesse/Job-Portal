package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.server.ResponseStatusException;
// import org.springframework.security.access.prepost.PreAuthorize;

// import com.example.demo.dto.SeekerProfileDTO;
import com.example.demo.dto.SeekerProfileUpdateDTO;
import com.example.demo.exception.SeekerNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Seeker;
// import com.example.demo.service.JobSeekerService;
import com.example.demo.security.JwtUtil;
import com.example.demo.model.User; // Add this import
import com.example.demo.model.UserType; // Add this import

//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;

import com.example.demo.repository.SeekerRepository;
import com.example.demo.repository.UserRepository;

//import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//import javax.crypto.SecretKey;
//import java.util.Base64;
//import java.util.Collections;
import java.util.Map; // Add this import

import org.slf4j.Logger; // Add this import
import org.slf4j.LoggerFactory; // Add this import

@RestController
@RequestMapping("/api/jobseeker")
@RequiredArgsConstructor

public class JobSeekerController {
        private static final Logger log = LoggerFactory.getLogger(JobSeekerController.class); // Add this line
        private final JwtUtil jwtUtil; // Add this line
        private final SeekerRepository seekerRepository;
        private final UserRepository userRepository; // Add this line

        @PutMapping("/profile")
        @PreAuthorize("hasAuthority('JOBSEEKER')")
        public ResponseEntity<?> updateSeekerProfile(@RequestBody SeekerProfileUpdateDTO profileDTO,
                        @RequestHeader("Authorization") String token) {
                try {
                        String email = jwtUtil.extractEmail(token.substring(7));
                        log.info("Seeker profile update request for user: {}", email);

                        // Find User
                        User user = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new UserNotFoundException("User  not found"));

                        // Ensure User is a Job Seeker
                        if (user.getRole() != UserType.JOBSEEKER) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(Map.of("error",
                                                                "Access Denied: Only Job Seekers can update their profile"));
                        }

                        // Find Seeker Profile
                        Seeker seeker = seekerRepository.findByUser(user)
                                        .orElseThrow(() -> new SeekerNotFoundException("Seeker profile not found"));

                        // Update Profile Fields if they are not null
                        if (profileDTO.getFullName() != null) {
                                seeker.setFullName(profileDTO.getFullName());
                        }
                        if (profileDTO.getPhone() != null) {
                                seeker.setPhone(profileDTO.getPhone());
                        }
                        if (profileDTO.getResume() != null) {
                                seeker.setResume(profileDTO.getResume());
                        }
                        if (profileDTO.getSkills() != null) {
                                seeker.setSkills(profileDTO.getSkills());
                        }
                        if (profileDTO.getExperience() != null) {
                                seeker.setExperience(profileDTO.getExperience());
                        }

                        // Save Updated Profile
                        seekerRepository.save(seeker);
                        log.info("Seeker profile updated successfully for user: {}", email);

                        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
                } catch (UserNotFoundException | SeekerNotFoundException e) {
                        log.error("Profile update error: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
                } catch (Exception e) {
                        log.error("Seeker profile update failed", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error", "Profile update failed"));
                }
        }

        @GetMapping("/profile")
        @PreAuthorize("hasAuthority('JOBSEEKER')")
        public ResponseEntity<?> getSeekerProfile(@RequestHeader("Authorization") String token) {
                try {
                        String email = jwtUtil.extractEmail(token.substring(7));
                        User user = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new UserNotFoundException("User not found"));

                        Seeker seeker = seekerRepository.findByUser(user)
                                        .orElseThrow(() -> new SeekerNotFoundException("Seeker profile not found"));

                        return ResponseEntity.ok(Map.of(
                                        "fullName", seeker.getFullName(),
                                        "phone", seeker.getPhone(),
                                        "skills", seeker.getSkills(),
                                        "experience", seeker.getExperience(),
                                        "resume", seeker.getResume()));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error", "Failed to load profile"));
                }
        }

        /*
         * private Long extractSeekerIdFromToken(String token) {
         * // Decode the base64-encoded secret key
         * SecretKey secretKey = Keys
         * .hmacShaKeyFor(Base64.getDecoder()
         * .decode("Vyz1ekXuIXbhI7RVtm0BzcahfJFegPls003YD3f1C3Q="));
         * 
         * Claims claims = Jwts.parserBuilder()
         * .setSigningKey(secretKey) // Use the SecretKey instead of a String
         * .build()
         * .parseClaimsJws(token.replace("Bearer ", ""))
         * .getBody();
         * return Long.valueOf(claims.get("seekerId").toString());
         * }
         */
}
