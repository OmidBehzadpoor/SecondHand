package com.example.secondhand.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
}