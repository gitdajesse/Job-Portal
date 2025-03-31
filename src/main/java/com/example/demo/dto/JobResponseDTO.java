package com.example.demo.dto;

import com.example.demo.model.Job;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class JobResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String salary;
    private String jobType;

    public JobResponseDTO(Job job) {
        this.id = job.getId();
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.location = job.getLocation();
        this.salary = job.getSalary();
        this.jobType = job.getJobType().toString(); // Ensure jobType is returned as a string
    }
}