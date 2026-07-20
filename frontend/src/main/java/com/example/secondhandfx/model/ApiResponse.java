package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String messageCode;
    private T data;
}