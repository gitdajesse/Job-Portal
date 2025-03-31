package com.demo;

// import com.example.demo.EmploymentMatchingSystemApplication;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class) // Add this import

public class JobRepositoryTest {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveJob() {
        // Create and save user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setRole(UserType.EMPLOYER);
        user = userRepository.save(user);

        // Create and save employer
        Employer employer = new Employer();
        employer.setCompanyName("Test Company");
        employer.setLocation("Test Location");
        employer.setUser(user);
        employer = employerRepository.save(employer);

        // Create and save job
        Job job = new Job();
        job.setTitle("Software Engineer");
        job.setDescription("Develop awesome software");
        job.setLocation("Remote");
        job.setEmployer(employer);
        Job savedJob = jobRepository.save(job);

        // Test retrieval
        Job foundJob = jobRepository.findById(savedJob.getId()).orElse(null);

        // Assertions
        assertNotNull(foundJob);
        assertEquals("Software Engineer", foundJob.getTitle());
        assertEquals("Develop awesome software", foundJob.getDescription());
        assertEquals("Remote", foundJob.getLocation());
        assertNotNull(foundJob.getEmployer());
        assertEquals("Test Company", foundJob.getEmployer().getCompanyName());
    }
}
