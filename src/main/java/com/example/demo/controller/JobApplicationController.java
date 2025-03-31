package com.example.demo.controller;

import com.example.demo.model.JobApplication;
import com.example.demo.model.ApplicationStatus;
import com.example.demo.model.User;
import com.example.demo.model.Job;
import com.example.demo.model.Seeker;
import com.example.demo.repository.JobApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.SeekerRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/job-applications")

public class JobApplicationController {

        @Autowired
        private JobApplicationRepository jobApplicationRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JobRepository jobRepository;

        @Autowired
        private SeekerRepository seekerRepository;

        @Autowired
        private JwtUtil jwtUtil;

        // Allowed status transitions
        private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS = Map.of(
                        ApplicationStatus.PENDING, Set.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED),
                        ApplicationStatus.ACCEPTED, Set.of(ApplicationStatus.ACCEPTED),
                        ApplicationStatus.REJECTED, Set.of() // No transitions allowed
        );

        // Apply for a job
        @PostMapping
        @PreAuthorize("hasAuthority('JOBSEEKER')")
        public ResponseEntity<?> applyForJob(
                        @RequestBody Map<String, Object> request,
                        @RequestHeader(value = "Authorization", required = false) String authHeader) {

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Authorization header is required"));
                }

                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);

                User seekerUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Seeker seeker = seekerRepository.findByUser(seekerUser)
                                .orElseThrow(() -> new RuntimeException("Seeker profile not found"));

                Long jobId = request.get("jobId") != null ? ((Number) request.get("jobId")).longValue() : null;
                String coverLetter = (String) request.get("coverLetter");

                // ✅ Validate Job ID
                if (jobId == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Job ID is required"));
                }

                // ✅ Validate Cover Letter
                if (coverLetter == null || coverLetter.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Cover letter is required"));
                }

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                JobApplication newApplication = new JobApplication();
                newApplication.setSeeker(seeker);
                newApplication.setJob(job);
                newApplication.setCoverLetter(coverLetter);
                newApplication.setStatus(ApplicationStatus.PENDING);

                jobApplicationRepository.save(newApplication);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(Map.of("message", "Application submitted successfully"));
        }

        // Update job application status with validation
        @PutMapping("/{applicationId}/status")
        @PreAuthorize("hasAuthority('EMPLOYER')")
        public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId,
                        @RequestBody Map<String, String> status) {
                JobApplication application = jobApplicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("Application not found"));

                ApplicationStatus newStatus;
                try {
                        newStatus = ApplicationStatus.valueOf(status.get("status"));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("error", "Invalid status value"));
                }

                ApplicationStatus currentStatus = application.getStatus();

                // Validate transition
                if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("error", "Invalid status transition from " + currentStatus + " to "
                                                        + newStatus));
                }

                application.setStatus(newStatus);
                jobApplicationRepository.save(application);

                return ResponseEntity.ok(Map.of("message", "Application status updated successfully"));
        }

        // Delete job application
        @DeleteMapping("/{applicationId}")
        @PreAuthorize("hasAuthority('JOBSEEKER')")
        public ResponseEntity<?> deleteApplication(@PathVariable Long applicationId) {
                jobApplicationRepository.deleteById(applicationId);
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Application deleted successfully"));
        }
}
