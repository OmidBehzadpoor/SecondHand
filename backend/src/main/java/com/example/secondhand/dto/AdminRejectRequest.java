package com.example.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRejectRequest {

    @NotBlank(message = "دلیل رد نمی‌تواند خالی باشد")
    private String reason;
}