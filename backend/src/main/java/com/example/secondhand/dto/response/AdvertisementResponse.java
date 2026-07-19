package com.example.secondhand.dto.response;

import com.example.secondhand.model.AdvertisementStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementResponse
{
    private Long id;
    private String title;
    private String description;
    private Long price;
    private String cityName;
    private String categoryName;
    private AdvertisementStatus status;
    private Long ownerId;
    private String ownerUsername;
    private Double sellerAverageRating;
    private Long sellerRatingCount;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}