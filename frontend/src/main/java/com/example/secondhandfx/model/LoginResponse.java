package com.example.secondhandfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String name; // to be printed in the messages shown to the user
    private String token;
    private Long userId;
    private String username;
    private Role role;
}