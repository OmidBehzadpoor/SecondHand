package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.SellerRatingRequest;
import com.example.secondhandfx.model.SellerRatingResponse;

import java.util.List;

public interface RatingService {

    SellerRatingResponse rateAdvertisement(Long advertisementId, SellerRatingRequest request) throws ApiException;

    List<SellerRatingResponse> getSellerRatings(Long sellerId) throws ApiException;

    Double getSellerAverageRating(Long sellerId) throws ApiException;
}