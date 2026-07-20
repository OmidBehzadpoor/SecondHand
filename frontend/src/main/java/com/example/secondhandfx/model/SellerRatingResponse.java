package com.example.secondhandfx.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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