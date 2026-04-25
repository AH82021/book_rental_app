package com.bookstore.user.controller;

import com.bookstore.user.dto.UserProfileResponse;
import com.bookstore.user.dto.UserProfileUpdateRequest;
import com.bookstore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return null;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        return null;
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return null;
    }
    
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        return null;
    }
}
