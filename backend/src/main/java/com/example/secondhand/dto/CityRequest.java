package com.example.secondhand.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityRequest {
    @NotBlank(message = "نام شهر نمیتواند خالی باشد")
    private String name;
}
