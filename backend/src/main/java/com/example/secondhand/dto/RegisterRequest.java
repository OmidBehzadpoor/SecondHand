package com.example.secondhand.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String name;
    private String username;
    private String password;
    private String phone;
    private String email;
    // no id, role or status; because client shouldn't have access to the mentioned fields
}
