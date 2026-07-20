package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.FavoriteResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class FavoriteServiceImpl implements FavoriteService {

    @Override
    public FavoriteResponse addFavorite(Long advertisementId) throws ApiException {
        ApiResponse<FavoriteResponse> response = HttpClientHelper.post(
                "/api/favorites/" + advertisementId,
                null,
                new TypeReference<ApiResponse<FavoriteResponse>>() {}
        );
        return response.getData();
    }

    @Override
    public void removeFavorite(Long advertisementId) throws ApiException {
        HttpClientHelper.delete(
                "/api/favorites/" + advertisementId,
                new TypeReference<ApiResponse<Void>>() {}
        );
    }

    @Override
    public List<FavoriteResponse> getMyFavorites() throws ApiException {
        ApiResponse<List<FavoriteResponse>> response = HttpClientHelper.get(
                "/api/favorites",
                new TypeReference<ApiResponse<List<FavoriteResponse>>>() {}
        );
        return response.getData();
    }
}