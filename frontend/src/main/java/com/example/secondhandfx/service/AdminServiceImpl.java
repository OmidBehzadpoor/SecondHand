package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdminAdvertisementResponse;
import com.example.secondhandfx.model.AdminRejectRequest;
import com.example.secondhandfx.model.AdminUserResponse;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class AdminServiceImpl implements AdminService {

    @Override
    public List<AdminAdvertisementResponse> getPendingAdvertisements() throws ApiException {
        return HttpClientHelper.get(
                "/api/admin/advertisements/pending",
                new TypeReference<ApiResponse<List<AdminAdvertisementResponse>>>() {}
        ).getData();
    }

    @Override
    public AdminAdvertisementResponse approveAdvertisement(Long id) throws ApiException {
        return HttpClientHelper.patch(
                "/api/admin/advertisements/" + id + "/approve",
                null,
                new TypeReference<ApiResponse<AdminAdvertisementResponse>>() {}
        ).getData();
    }

    @Override
    public AdminAdvertisementResponse rejectAdvertisement(Long id, String reason) throws ApiException {
        AdminRejectRequest request = AdminRejectRequest.builder()
                .reason(reason)
                .build();

        return HttpClientHelper.patch(
                "/api/admin/advertisements/" + id + "/reject",
                request,
                new TypeReference<ApiResponse<AdminAdvertisementResponse>>() {}
        ).getData();
    }

    @Override
    public void deleteAdvertisement(Long id) throws ApiException {
        HttpClientHelper.delete(
                "/api/admin/advertisements/" + id,
                new TypeReference<ApiResponse<Void>>() {}
        );
    }

    @Override
    public List<AdminUserResponse> getAllUsers() throws ApiException {
        return HttpClientHelper.get(
                "/api/admin/users",
                new TypeReference<ApiResponse<List<AdminUserResponse>>>() {}
        ).getData();
    }

    @Override
    public AdminUserResponse blockUser(Long userId) throws ApiException {
        return HttpClientHelper.patch(
                "/api/admin/users/" + userId + "/block",
                null,
                new TypeReference<ApiResponse<AdminUserResponse>>() {}
        ).getData();
    }

    @Override
    public AdminUserResponse unblockUser(Long userId) throws ApiException {
        return HttpClientHelper.patch(
                "/api/admin/users/" + userId + "/unblock",
                null,
                new TypeReference<ApiResponse<AdminUserResponse>>() {}
        ).getData();
    }
}