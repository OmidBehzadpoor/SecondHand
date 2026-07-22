package com.example.secondhand.dto.response;

import com.example.secondhand.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String name;
    private String token;
    private Long userId;
    private String username;
    private Role role;
}