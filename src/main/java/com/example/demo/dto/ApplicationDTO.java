package com.example.demo.dto;

import com.example.demo.model.JobApplication;
import com.example.demo.model.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter

public class ApplicationDTO {
    private Long id;
    private String jobTitle;
    private String applicant;
    private String applicantEmail;
    private String coverLetter;
    private String skills;
    private String experience;
    private ApplicationStatus status;
    private Date createdAt;

    public ApplicationDTO(Long id, String jobTitle, String applicant, String applicantEmail,
            String coverLetter, String skills, String experience,
            String status, Date createdAt) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.applicant = applicant;
        this.applicantEmail = applicantEmail;
        this.coverLetter = coverLetter;
        this.skills = skills;
        this.experience = experience;
        this.status = ApplicationStatus.valueOf(status); // Convert String to Enum
        this.createdAt = createdAt;
    }

    public ApplicationDTO(JobApplication application) {
        this.id = application.getId();
        this.jobTitle = application.getJob().getTitle();
        this.applicant = application.getSeeker().getFullName();
        this.applicantEmail = application.getSeeker().getUser().getEmail();
        this.coverLetter = application.getCoverLetter();
        this.skills = application.getSeeker().getSkills() != null ? application.getSeeker().getSkills()
                : "Not provided";
        this.experience = application.getSeeker().getExperience() != null ? application.getSeeker().getExperience()
                : "Not provided";
        this.status = application.getStatus();
        this.createdAt = application.getApplicationDate();
    }
}
