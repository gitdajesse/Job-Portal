package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class EmployerProfileUpdateDTO {
    @Size(min = 2, max = 100, message = "Company name must be between 2-100 characters")
    private String companyName;

    @Size(min = 20, max = 1000, message = "Company info must be between 20-1000 characters")
    private String companyInfo;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    @Size(min = 2, max = 100, message = "Location must be between 2-100 characters")
    private String location;
    // Getters and setters
}
