package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SeekerProfileDTO {
    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Phone number cannot be empty")
    private String phone;

    private String resume; // Optional field

    @NotBlank(message = "Skills cannot be empty")
    private String skills;

    @NotBlank(message = "Experience cannot be empty")
    private String experience;
}
