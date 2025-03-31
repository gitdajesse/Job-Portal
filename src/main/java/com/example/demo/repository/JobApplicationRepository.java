package com.example.demo.repository;

import com.example.demo.model.JobApplication;
import com.example.demo.model.ApplicationStatus;
import com.example.demo.model.Job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByStatus(ApplicationStatus status);

    List<JobApplication> findByJobIn(List<Job> jobs); // Fetch applications for jobs

    Page<JobApplication> findByJobIn(List<Job> jobs, Pageable pageable);

    Page<JobApplication> findByJobInAndStatus(List<Job> jobs, ApplicationStatus status, Pageable pageable);

    List<JobApplication> findByJob_EmployerId(Long employerId);

    List<JobApplication> findByJob_EmployerIdAndStatus(Long employerId, ApplicationStatus status);

}
