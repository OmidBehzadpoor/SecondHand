package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;

    private long totalAdvertisements;
    private long pendingAdvertisements;
    private long approvedAdvertisements;
    private long rejectedAdvertisements;
    private long soldAdvertisements;
    private long deletedAdvertisements;

    private long totalCategories;
    private long totalCities;

    private long totalConversations;
    private long totalMessages;
    private long totalFavorites;
    private long totalRatings;
}