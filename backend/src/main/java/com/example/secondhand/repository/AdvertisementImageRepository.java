package com.example.secondhand.repository;

import com.example.secondhand.model.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
}