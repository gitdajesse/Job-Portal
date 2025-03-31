package com.example.demo.repository;

import com.example.demo.model.Job;
import com.example.demo.model.JobStatus;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional; // ✅ Correct import for Jakarta EE

import java.util.List;

@Repository

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.status = :status WHERE j.id = :jobId")
    int updateJobStatus(@Param("jobId") Long jobId, @Param("status") JobStatus status);

    List<Job> findByEmployer(User employer); // This allows finding jobs by employer

    List<Job> findByLocation(String location);
}
