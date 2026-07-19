package com.example.secondhand.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdvertisementImageResponse {
    private Long id;
    private String imageUrl;
}