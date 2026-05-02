package com.bookstore.auth.controller;

import com.bookstore.auth.dto.AuthResponse;
import com.bookstore.auth.dto.LoginRequest;
import com.bookstore.auth.dto.RegisterRequest;
import com.bookstore.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {

  @Autowired
    private AuthService authService;


  @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
      try {

          AuthResponse response = authService.register(registerRequest);
          return ResponseEntity.ok(response);
      } catch (RuntimeException e) {
          return ResponseEntity.badRequest().build();
      }
  }



  @PostMapping("/login")
      public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {

          try {

              AuthResponse response = authService.login(loginRequest);
              return ResponseEntity.ok(response);
          } catch (RuntimeException e) {
              return ResponseEntity.status(401).build();
          }

      }


}
