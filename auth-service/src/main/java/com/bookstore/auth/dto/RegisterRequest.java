package com.bookstore.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    public  String email;
    public  String password;// TOm2023
    public String firstName;
    public String lastName;
    public String adminCode;
}
