package com.bookstore.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "users")
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
    private Role role = Role.USER;

  @CreationTimestamp
  @Column(nullable = false)
    private LocalDateTime createTime;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;



    public enum Role {
        USER,
        ADMIN
    }

}
