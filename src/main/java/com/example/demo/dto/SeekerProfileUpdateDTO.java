package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeekerProfileUpdateDTO {
    private String fullName; // Optional field
    private String phone; // Optional field
    private String resume; // Optional field
    private String skills; // Optional field
    private String experience; // Optional field
}