package com.example.demo.controller;

import com.example.demo.dto.EmployerProfileUpdateDTO;
import com.example.demo.exception.EmployerNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Employer;
import com.example.demo.model.User;
import com.example.demo.model.UserType;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;

import com.example.demo.dto.ApplicationDTO;
import com.example.demo.service.JobApplicationService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/employer")
@PreAuthorize("hasRole('EMPLOYER')") // Ensure roles are correctly formatted in JWT

public class EmployerController {
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public EmployerController(EmployerRepository employerRepository,
            UserRepository userRepository,
            JwtUtil jwtUtil) {
        this.employerRepository = employerRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    private JobApplicationService jobApplicationService;

    @GetMapping("/applications") // Add this to expose it as an endpoint
    public ResponseEntity<List<ApplicationDTO>> getApplications(@RequestParam(required = false) String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            List<ApplicationDTO> applications = jobApplicationService.getApplicationsForEmployer(username, status);
            return ResponseEntity.ok(applications);
        } else {
            throw new RuntimeException("User is not authenticated");
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAuthority('EMPLOYER')")
    public ResponseEntity<?> updateEmployerProfile(@Valid @RequestBody EmployerProfileUpdateDTO profileDTO,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String token) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        try {
            String email = jwtUtil.extractEmail(token.substring(7));

            // Find User
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Ensure User is an Employer
            if (user.getRole() != UserType.EMPLOYER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access Denied: Only Employers can update their profile"));
            }

            // Find Employer Profile
            Employer employer = employerRepository.findByUser(user)
                    .orElseThrow(() -> new EmployerNotFoundException("Employer profile not found"));

            // Update Profile Fields if they are not null
            if (profileDTO.getCompanyName() != null) {
                employer.setCompanyName(profileDTO.getCompanyName());
            }
            if (profileDTO.getCompanyInfo() != null) {
                employer.setCompanyInfo(profileDTO.getCompanyInfo());
            }
            if (profileDTO.getPhone() != null) {
                employer.setPhone(profileDTO.getPhone());
            }
            if (profileDTO.getLocation() != null) {
                employer.setLocation(profileDTO.getLocation());
            }

            // Save Updated Profile
            employerRepository.save(employer);

            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (UserNotFoundException | EmployerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Profile update failed"));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('EMPLOYER')")
    public ResponseEntity<?> getEmployerProfile(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.substring(7));
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Employer employer = employerRepository.findByUser(user)
                    .orElseThrow(() -> new EmployerNotFoundException("Employer profile not found"));

            return ResponseEntity.ok(Map.of(
                    "companyName", employer.getCompanyName(),
                    "companyInfo", employer.getCompanyInfo(),
                    "phone", employer.getPhone(),
                    "location", employer.getLocation()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load profile"));
        }
    }
}
