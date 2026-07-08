package com.example.secondhand.repository;

import com.example.secondhand.model.SellerRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRatingRepository extends JpaRepository<SellerRating, Long> {

    boolean existsByBuyerIdAndAdvertisementId(Long buyerId, Long advertisementId);

    List<SellerRating> findByAdvertisementSellerId(Long sellerId);

}
