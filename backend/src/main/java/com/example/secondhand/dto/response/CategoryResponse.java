package com.example.secondhand.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse
{
    private Long id;
    private String name;
}
