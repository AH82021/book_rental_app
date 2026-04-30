package com.bookstore.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class AuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

// Security filter chain : This list of rules who can access what
// PasswordEncoder  : This allows us to hash the passwords before saving on database
// Statless-Session : This is to make sure that we are not using session to store the user information, instead we will use JWT token
//GET /api/v1/books/*" , permitAll()
// Delete /api/v1/books/*".  authenticated()
// csrf ,


// username, passwrod  , JWT (Jason Web Token)
//"Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6MSwic3ViIjoidXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc3NzMyMzU1OCwiZXhwIjoxNzc3NDA5OTU4fQ.yG3eY02_KZ9wpH97xqr-zRROxy2Dg2PH_B1N2mZ5iSU"


