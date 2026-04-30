package com.bookstore.auth.dto;

import com.bookstore.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromUser(User user) {
        return  new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getFirstName(),
                user.getRole(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
