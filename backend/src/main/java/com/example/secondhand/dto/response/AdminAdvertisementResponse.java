package com.example.secondhand.dto.response;

import com.example.secondhand.model.AdvertisementStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminAdvertisementResponse {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private String categoryName;
    private String cityName;
    private AdvertisementStatus status;
    private Long sellerId;
    private String sellerUsername;
    private String sellerName;
    private String sellerPhone;
    private String sellerEmail;
    private List<String> imageUrls;
    private String rejectionReason;
    private LocalDateTime createdAt;
}