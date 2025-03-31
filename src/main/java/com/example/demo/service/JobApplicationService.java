package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.model.JobApplication;
import com.example.demo.model.User;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.JobApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.ApplicationDTO;
import com.example.demo.exception.EmployerNotFoundException;
import com.example.demo.model.ApplicationStatus;
import com.example.demo.model.Employer;

@Service

public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;

    @Autowired
    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
            UserRepository userRepository,
            EmployerRepository employerRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.employerRepository = employerRepository;
    }

    public List<ApplicationDTO> getApplicationsForEmployer(String employerEmail, String status) {
        // ✅ Find user by email (previously called employerUsername)
        User employerUser = userRepository.findByEmail(employerEmail)
                .orElseThrow(
                        () -> new EmployerNotFoundException("Employer user not found with email: " + employerEmail));

        // ✅ Find employer associated with this user
        Employer employer = employerRepository.findByUser(employerUser)
                .orElseThrow(() -> new EmployerNotFoundException("Employer not found for user: " + employerEmail));

        List<JobApplication> applications;

        if (status != null) {
            try {
                ApplicationStatus applicationStatus = ApplicationStatus.valueOf(status.toUpperCase());
                applications = jobApplicationRepository.findByJob_EmployerIdAndStatus(employer.getId(),
                        applicationStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        } else {
            applications = jobApplicationRepository.findByJob_EmployerId(employer.getId());
        }

        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ApplicationDTO convertToDTO(JobApplication application) {
        return new ApplicationDTO(
                application.getId(),
                application.getJob() != null ? application.getJob().getTitle() : "Unknown Job",
                application.getSeeker() != null ? application.getSeeker().getFullName() : "Unknown Seeker",
                application.getSeeker() != null && application.getSeeker().getUser() != null
                        ? application.getSeeker().getUser().getEmail()
                        : "Unknown Email",
                application.getCoverLetter(),
                application.getSeeker() != null ? application.getSeeker().getSkills() : "No Skills",
                application.getSeeker() != null
                        ? application.getSeeker().getExperience() + " years" // ✅ Ensure String format
                        : "Not provided",
                application.getStatus() != null ? application.getStatus().name() : "UNKNOWN",
                application.getApplicationDate());
    }
}
