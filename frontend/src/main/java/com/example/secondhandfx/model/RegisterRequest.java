package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest implements ApiRequest {
    private String name;
    private String username;
    private String password;
    private String phone;
    private String email;
}
