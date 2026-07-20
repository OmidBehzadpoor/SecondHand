package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.SellerRatingRequest;
import com.example.secondhandfx.model.SellerRatingResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class RatingServiceImpl implements RatingService {

    @Override
    public SellerRatingResponse rateAdvertisement(Long advertisementId, SellerRatingRequest request) throws ApiException {
        return HttpClientHelper.post(
                "/api/ratings/advertisements/" + advertisementId,
                request,
                new TypeReference<ApiResponse<SellerRatingResponse>>() {}
        ).getData();
    }

    @Override
    public List<SellerRatingResponse> getSellerRatings(Long sellerId) throws ApiException {
        return HttpClientHelper.get(
                "/api/ratings/sellers/" + sellerId,
                new TypeReference<ApiResponse<List<SellerRatingResponse>>>() {}
        ).getData();
    }

    @Override
    public Double getSellerAverageRating(Long sellerId) throws ApiException {
        return HttpClientHelper.get(
                "/api/ratings/sellers/" + sellerId + "/average",
                new TypeReference<ApiResponse<Double>>() {}
        ).getData();
    }
}