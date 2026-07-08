package com.example.secondhand.repository;

import com.example.secondhand.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndAdvertisementId(Long userId, Long advertisementId);

    void deleteByUserIdAndAdvertisementId(Long userId, Long advertisementId);
}