package com.example.secondhand.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class SellerRatingSummary {

    public static final SellerRatingSummary EMPTY =
            SellerRatingSummary.builder().averageRating(0.0).ratingCount(0L).build();

    private final Double averageRating;
    private final Long ratingCount;
}