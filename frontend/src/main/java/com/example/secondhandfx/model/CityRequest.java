package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityRequest implements ApiRequest {
    private String name;
}