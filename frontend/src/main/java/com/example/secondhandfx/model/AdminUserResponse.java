package com.example.secondhandfx.model;

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
    private String userStatus;
}