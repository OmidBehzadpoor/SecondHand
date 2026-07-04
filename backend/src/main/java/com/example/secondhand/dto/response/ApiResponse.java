package com.example.secondhand.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String messageCode;
    private T data;
}
