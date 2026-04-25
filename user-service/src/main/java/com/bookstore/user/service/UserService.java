package com.bookstore.user.service;

import com.bookstore.user.dto.UserProfileResponse;
import com.bookstore.user.dto.UserProfileUpdateRequest;
import com.bookstore.user.model.UserProfile;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public List<UserProfileResponse> getAllUsers() {

        return  null;
    }
    
    public UserProfileResponse getUserProfile(Long id) {
         return null;
    }
    
    @Transactional
    public UserProfileResponse updateUserProfile(Long id, UserProfileUpdateRequest request) {
        return null;
    }
    
    // For creating user when registered via Auth Service (would be called via event/message)
    @Transactional
    public UserProfileResponse createUserProfile(String email, String firstName, String lastName) {
        return null;
    }
    
    private UserProfileResponse mapToResponse(UserProfile user) {
        return null;
    }
}
