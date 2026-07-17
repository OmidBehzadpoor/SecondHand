package com.example.secondhand.dto.response;

import com.example.secondhand.model.Role;
import com.example.secondhand.model.UserStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private String email;
    private Role role;
    private UserStatus userStatus;
}