package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdminAdvertisementResponse;
import com.example.secondhandfx.model.AdminDashboardResponse;
import com.example.secondhandfx.model.AdminUserResponse;

import java.util.List;

public interface AdminService {

    AdminDashboardResponse getDashboard() throws ApiException;

    List<AdminAdvertisementResponse> getPendingAdvertisements() throws ApiException;

    AdminAdvertisementResponse approveAdvertisement(Long id) throws ApiException;

    AdminAdvertisementResponse rejectAdvertisement(Long id, String reason) throws ApiException;

    void deleteAdvertisement(Long id) throws ApiException;

    List<AdminUserResponse> getAllUsers() throws ApiException;

    AdminUserResponse blockUser(Long userId) throws ApiException;

    AdminUserResponse unblockUser(Long userId) throws ApiException;
}