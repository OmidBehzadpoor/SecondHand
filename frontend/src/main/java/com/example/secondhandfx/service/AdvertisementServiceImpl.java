package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.PageResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.secondhandfx.model.AdvertisementImageResponse;
import com.example.secondhandfx.model.AdvertisementRequest;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AdvertisementServiceImpl implements AdvertisementService {

    @Override
    public PageResponse<AdvertisementResponse> getAll(String keyword,
                                                      Long categoryId,
                                                      Long cityId,
                                                      Long minPrice,
                                                      Long maxPrice,
                                                      String sortBy,
                                                      int page,
                                                      int size) throws ApiException {
        StringBuilder path = new StringBuilder("/api/advertisements?page=" + page + "&size=" + size);

        if (keyword != null && !keyword.isBlank()) {
            path.append("&keyword=").append(encode(keyword));
        }
        if (categoryId != null) {
            path.append("&categoryId=").append(categoryId);
        }
        if (cityId != null) {
            path.append("&cityId=").append(cityId);
        }
        if (minPrice != null) {
            path.append("&minPrice=").append(minPrice);
        }
        if (maxPrice != null) {
            path.append("&maxPrice=").append(maxPrice);
        }
        if (sortBy != null && !sortBy.isBlank()) {
            path.append("&sortBy=").append(sortBy);
        }

        ApiResponse<PageResponse<AdvertisementResponse>> response = HttpClientHelper.get(
                path.toString(),
                new TypeReference<ApiResponse<PageResponse<AdvertisementResponse>>>() {}
        );

        return response.getData();
    }

    @Override
    public AdvertisementResponse getById(Long id) throws ApiException {
        ApiResponse<AdvertisementResponse> response = HttpClientHelper.get(
                "/api/advertisements/" + id,
                new TypeReference<ApiResponse<AdvertisementResponse>>() {}
        );
        return response.getData();
    }

    @Override
    public void delete(Long id) throws ApiException {
        HttpClientHelper.delete("/api/advertisements/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    @Override
    public AdvertisementResponse markAsSold(Long id) throws ApiException {
        ApiResponse<AdvertisementResponse> response = HttpClientHelper.patch(
                "/api/advertisements/" + id + "/sold",
                null,
                new TypeReference<ApiResponse<AdvertisementResponse>>() {}
        );
        return response.getData();
    }

    @Override
    public AdvertisementResponse create(AdvertisementRequest request) throws ApiException {
        ApiResponse<AdvertisementResponse> response = HttpClientHelper.post(
                "/api/advertisements",
                request,
                new TypeReference<ApiResponse<AdvertisementResponse>>() {}
        );
        return response.getData();
    }

    @Override
    public AdvertisementImageResponse uploadImage(Long advertisementId, File file) throws ApiException {
        ApiResponse<AdvertisementImageResponse> response = HttpClientHelper.uploadFile(
                "/api/advertisements/" + advertisementId + "/images",
                file,
                new TypeReference<ApiResponse<AdvertisementImageResponse>>() {}
        );
        return response.getData();
    }

    @Override
    public List<AdvertisementResponse> getMyAdvertisements() throws ApiException {
        ApiResponse<List<AdvertisementResponse>> response = HttpClientHelper.get(
                "/api/advertisements/mine",
                new TypeReference<ApiResponse<List<AdvertisementResponse>>>() {}
        );
        return response.getData();
    }

    @Override
    public AdvertisementResponse update(Long id, AdvertisementRequest request) throws ApiException {
        ApiResponse<AdvertisementResponse> response = HttpClientHelper.put(
                "/api/advertisements/" + id,
                request,
                new TypeReference<ApiResponse<AdvertisementResponse>>() {}
        );
        return response.getData();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}