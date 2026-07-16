package com.example.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    @NotBlank(message = "متن پیام نمی‌تواند خالی باشد")
    @Size(max = 2000, message = "متن پیام نمی‌تواند بیشتر از ۲۰۰۰ کاراکتر باشد")
    private String content;
}