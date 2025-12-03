package com.hikehub.backend.service;

import com.hikehub.backend.model.User;
import com.hikehub.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Stub method to get the current authenticated user.
     * For now, tries to return user with ID = 1, or the first user if ID 1 doesn't exist.
     * This should be replaced with real authentication logic later.
     */
    public User getCurrentUser() {
        return userRepository.findById(1L)
                .orElseGet(() -> userRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Current user not found. Please ensure at least one user exists.")));
    }
}

