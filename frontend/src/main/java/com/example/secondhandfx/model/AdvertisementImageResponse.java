package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementImageResponse {
    private Long id;
    private String imageUrl;
}