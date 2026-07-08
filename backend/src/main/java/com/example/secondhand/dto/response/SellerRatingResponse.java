package com.example.secondhand.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SellerRatingResponse {
    private Long id;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private Long advertisementId;
    private String advertisementTitle;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}