package com.example.secondhandfx.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementRequest implements ApiRequest {
    private String title;
    private String description;
    private Long price;
    private Long categoryId;
    private Long cityId;
    private List<String> imageUrls;
}