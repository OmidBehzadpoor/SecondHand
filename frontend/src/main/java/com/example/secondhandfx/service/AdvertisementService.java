package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.model.PageResponse;

public interface AdvertisementService {

    PageResponse<AdvertisementResponse> getAll(String keyword,
                                                Long categoryId,
                                                Long cityId,
                                                Long minPrice,
                                                Long maxPrice,
                                                String sortBy,
                                                int page,
                                                int size) throws ApiException;

    AdvertisementResponse getById(Long id) throws ApiException;

    void delete(Long id) throws ApiException;

    AdvertisementResponse markAsSold(Long id) throws ApiException;
}