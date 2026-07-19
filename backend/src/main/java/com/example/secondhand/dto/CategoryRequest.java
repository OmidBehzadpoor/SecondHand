package com.example.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "نام دسته بندی نمیتواند خالی باشد")
    private String name;

    private Long parentId;
}