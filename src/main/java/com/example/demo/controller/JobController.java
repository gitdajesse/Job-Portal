package com.example.demo.controller;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import com.example.demo.dto.JobResponseDTO;
import com.example.demo.model.Employer;
import com.example.demo.model.JobStatus;
import com.example.demo.model.JobType;
import com.example.demo.service.JobService;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")

public class JobController {
    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Get all jobs
    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllJobs(
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "jobType", required = false) JobType jobType,
            @RequestParam(name = "minSalary", required = false) Integer minSalary) {

        List<Job> jobs;
        if (location != null || jobType != null || minSalary != null) {
            jobs = jobService.getFilteredJobs(location, jobType, minSalary);
        } else {
            jobs = jobService.getAllJobs();
        }

        List<JobResponseDTO> responseDTOs = jobs.stream()
                .map(JobResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    // Post a job (Employer only)
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<?> postJob(@RequestBody Job job, @RequestHeader("Authorization") String token) {
        // Validate job details
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job title is required"));
        }
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job description is required"));
        }

        String email = jwtUtil.extractEmail(token.substring(7));
        User employerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        Employer employer = employerRepository.findByUser(employerUser)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        job.setEmployer(employer);
        Job postedJob = jobService.postJob(job);

        // Return only relevant job details using DTO
        JobResponseDTO responseDTO = new JobResponseDTO(postedJob);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // Update job details
    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<?> updateJob(@PathVariable Long jobId, @RequestBody Job job,
            @RequestHeader("Authorization") String token) {
        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        existingJob.setTitle(job.getTitle());
        existingJob.setDescription(job.getDescription());
        existingJob.setLocation(job.getLocation());
        existingJob.setSalary(job.getSalary());
        existingJob.setJobType(job.getJobType());

        jobRepository.save(existingJob);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Job updated successfully"));
    }

    // Delete job
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<?> deleteJob(@PathVariable Long jobId, @RequestHeader("Authorization") String token) {
        jobRepository.deleteById(jobId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Job deleted successfully"));
    }

    // ✅ Update job status (Employer only)
    @PutMapping("/jobs/{jobId}/status")
    @PreAuthorize("hasAuthority('EMPLOYER')")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long jobId,
            @RequestBody Map<String, String> status,
            @RequestHeader("Authorization") String token) {
        // Retrieve job from database
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Extract employer email from JWT token
        String email = jwtUtil.extractEmail(token.substring(7));

        // Find the employer's user account
        User employerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the Employer entity linked to the User
        Employer employer = employerRepository.findByUser(employerUser)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        // Ensure that the employer updating the job is the one who posted it
        if (!job.getEmployer().equals(employer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Not authorized to update this job"));
        }

        // Validate status input
        String newStatus = status.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job status is required"));
        }

        try {
            job.setStatus(JobStatus.valueOf(newStatus.toUpperCase())); // Ensure case insensitivity
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid job status"));
        }

        // Save updated job status
        jobRepository.save(job);

        return ResponseEntity.ok(Map.of("message", "Job status updated successfully"));
    }
}
