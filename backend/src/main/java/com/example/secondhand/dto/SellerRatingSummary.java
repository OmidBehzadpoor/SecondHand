package com.example.secondhand.dto;


public record SellerRatingSummary(Double averageRating, Long ratingCount) {

    public static final SellerRatingSummary EMPTY = new SellerRatingSummary(0.0, 0L);
}