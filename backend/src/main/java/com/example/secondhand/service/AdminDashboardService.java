package com.example.secondhand.service;

import com.example.secondhand.dto.response.AdminDashboardResponse;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import com.example.secondhand.repository.CityRepository;
import com.example.secondhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .blockedUsers(userRepository.countByStatus(UserStatus.BLOCKED))
                .totalAdvertisements(advertisementRepository.count())
                .pendingAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.PENDING))
                .approvedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.APPROVED))
                .rejectedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.REJECTED))
                .soldAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.SOLD))
                .deletedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.DELETED))
                .totalCategories(categoryRepository.count())
                .totalCities(cityRepository.count())
                .build();
    }
}