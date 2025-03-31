package com.example.demo.repository;

import com.example.demo.model.Employer;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    /**
     * Find an employer by their associated user.
     *
     * @param user The user associated with the employer.
     * @return An Optional containing the employer if found, otherwise empty.
     */
    Optional<Employer> findByUser(User user);

    /**
     * Find an employer by their email (via the associated user).
     *
     * @param email The email of the user associated with the employer.
     * @return An Optional containing the employer if found, otherwise empty.
     */
    Optional<Employer> findByUser_Email(String email);

    /**
     * Check if an employer exists for the given user.
     *
     * @param user The user associated with the employer.
     * @return True if an employer exists for the user, otherwise false.
     */
    boolean existsByUser(User user);

    /**
     * Delete an employer by their associated user.
     *
     * @param user The user associated with the employer.
     */
    void deleteByUser(User user);
}
