package com.example.secondhand.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "نام نمی‌تواند خالی باشد")
    private String name;

    @NotBlank(message = "نام کاربری نمی‌تواند خالی باشد")
    private String username;

    @Size(min = 4, message = "رمز عبور باید حداقل ۴ کاراکتر باشد")
    private String password;

    @NotBlank(message = "شماره تماس نمی‌تواند خالی باشد")
    private String phone;

    @NotBlank(message = "ایمیل نمی‌تواند خالی باشد")
    @Email(message = "ایمیل معتبر نیست")
    private String email;

    private String captchaToken;
    // no id, role or status; because client shouldn't have access to the mentioned fields
}
