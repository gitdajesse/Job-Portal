package com.example.demo.repository;

import com.example.demo.model.Seeker;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeekerRepository extends JpaRepository<Seeker, Long> {

    /**
     * Find a seeker by their associated user.
     *
     * @param user The user associated with the seeker.
     * @return An Optional containing the seeker if found, otherwise empty.
     */
    Optional<Seeker> findByUser(User user);

    /**
     * Find a seeker by their email (via the associated user).
     *
     * @param email The email of the user associated with the seeker.
     * @return An Optional containing the seeker if found, otherwise empty.
     */
    Optional<Seeker> findByUser_Email(String email);

    /**
     * Check if a seeker exists for the given user.
     *
     * @param user The user associated with the seeker.
     * @return True if a seeker exists for the user, otherwise false.
     */
    boolean existsByUser(User user);

    /**
     * Delete a seeker by their associated user.
     *
     * @param user The user associated with the seeker.
     */
    void deleteByUser(User user);
}
