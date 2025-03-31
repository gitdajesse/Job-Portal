package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.SeekerProfileDTO;
import com.example.demo.model.Seeker;
import com.example.demo.repository.SeekerRepository;

@Service

public class JobSeekerService {
    private final SeekerRepository seekerRepository;

    // Constructor Injection
    public JobSeekerService(SeekerRepository seekerRepository) {
        this.seekerRepository = seekerRepository;
    }

    public void updateProfile(Long seekerId, SeekerProfileDTO jobSeekerDTO) {
        Seeker seeker = seekerRepository.findById(seekerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (jobSeekerDTO.getFullName() != null) {
            seeker.setFullName(jobSeekerDTO.getFullName());
        }
        if (jobSeekerDTO.getPhone() != null) {
            seeker.setPhone(jobSeekerDTO.getPhone());
        }
        if (jobSeekerDTO.getResume() != null) {
            seeker.setResume(jobSeekerDTO.getResume());
        }
        if (jobSeekerDTO.getSkills() != null) {
            seeker.setSkills(jobSeekerDTO.getSkills());
        }
        if (jobSeekerDTO.getExperience() != null) {
            seeker.setExperience(jobSeekerDTO.getExperience());
        }

        seekerRepository.save(seeker);
    }
}
