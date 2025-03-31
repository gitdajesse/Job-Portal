package com.example.demo.exception;

public class SeekerNotFoundException extends RuntimeException {
    public SeekerNotFoundException(String message) {
        super(message);
    }
}