package com.example.secondhandfx.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAdvertisementResponse {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private String categoryName;
    private String cityName;
    private String status;
    private Long sellerId;
    private String sellerUsername;
    private String sellerName;
    private String sellerPhone;
    private String sellerEmail;
    private Double sellerAverageRating;
    private Long sellerRatingCount;
    private List<String> imageUrls;
    private String rejectionReason;
    private LocalDateTime createdAt;
}