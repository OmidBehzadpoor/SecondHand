package com.example.secondhand.dto.response;

import com.example.secondhand.model.AdvertisementStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FavoriteResponse {
    private Long id;
    private Long advertisementId;
    private String advertisementTitle;
    private String advertisementDescription;
    private Long price;
    private String cityName;
    private String categoryName;
    private AdvertisementStatus advertisementStatus;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}