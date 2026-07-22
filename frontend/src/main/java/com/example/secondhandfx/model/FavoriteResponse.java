package com.example.secondhandfx.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteResponse {
    private Long id;
    private Long advertisementId;
    private String advertisementTitle;
    private String advertisementDescription;
    private Long price;
    private String cityName;
    private String categoryName;
    private String advertisementStatus;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}