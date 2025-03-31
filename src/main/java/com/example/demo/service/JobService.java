package com.example.demo.service;

import com.example.demo.model.Job;
import com.example.demo.repository.JobRepository;
import com.example.demo.model.JobStatus;
import com.example.demo.model.JobType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional; // ✅ Correct import for Jakarta EE

import java.util.List;

@Service

public class JobService {
    @Autowired
    private JobRepository jobRepository;

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job postJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getJobsByLocation(String location) {
        return jobRepository.findByLocation(location);
    }

    @Transactional
    public void updateJobStatus(Long jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        jobRepository.save(job);
    }

    public List<Job> getFilteredJobs(String location, JobType jobType, Integer minSalary) {
        Specification<Job> spec = Specification.where(null);

        if (location != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("location"), location));
        }

        if (jobType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("jobType"), jobType));
        }

        if (minSalary != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(
                    cb.function("replace", String.class, root.get("salary"), cb.literal("$"), cb.literal(""))
                            .as(Integer.class),
                    minSalary));
        }

        return jobRepository.findAll(spec);
    }
}
