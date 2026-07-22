package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.model.PageResponse;
import com.example.secondhandfx.model.AdvertisementImageResponse;
import com.example.secondhandfx.model.AdvertisementRequest;

import java.io.File;
import java.util.List;

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

    AdvertisementResponse create(AdvertisementRequest request) throws ApiException;

    AdvertisementImageResponse uploadImage(Long advertisementId, File file) throws ApiException;

    List<AdvertisementImageResponse> getImages(Long advertisementId) throws ApiException;

    void deleteImage(Long advertisementId, Long imageId) throws ApiException;

    List<AdvertisementResponse> getMyAdvertisements() throws ApiException;

    AdvertisementResponse update(Long id, AdvertisementRequest request) throws ApiException;
}