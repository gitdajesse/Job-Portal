package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String role; // Should be either "JOBSEEKER" or "EMPLOYER"

    // Additional fields for employer
    private String companyName;
    private String companyInfo;
    private String phone;
    private String location;

    // Additional fields for job seeker
    private String fullName;
    private String resume;
    private String skills;
    private String experience;
}