package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.FavoriteResponse;

import java.util.List;

public interface FavoriteService {

    FavoriteResponse addFavorite(Long advertisementId) throws ApiException;

    void removeFavorite(Long advertisementId) throws ApiException;

    List<FavoriteResponse> getMyFavorites() throws ApiException;
}