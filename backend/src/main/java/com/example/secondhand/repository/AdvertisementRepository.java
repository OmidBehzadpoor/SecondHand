package com.example.secondhand.repository;

import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByStatus(AdvertisementStatus status);

    List<Advertisement> findByStatusAndCategoryId(AdvertisementStatus status, Long categoryId);

    List<Advertisement> findByStatusAndCityId(AdvertisementStatus status, Long cityId);

    List<Advertisement> findByStatusAndCategoryIdAndCityId(AdvertisementStatus status, Long categoryId, Long cityId);

    List<Advertisement> findBySellerId(Long sellerId);
}