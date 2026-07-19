package com.example.secondhand.repository;

import com.example.secondhand.model.SellerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerRatingRepository extends JpaRepository<SellerRating, Long> {

    boolean existsByBuyerIdAndAdvertisementId(Long buyerId, Long advertisementId);

    List<SellerRating> findByAdvertisementSellerId(Long sellerId);


    @Query("SELECT sr.advertisement.seller.id AS sellerId, " +
           "AVG(sr.rating) AS averageRating, " +
           "COUNT(sr) AS ratingCount " +
           "FROM SellerRating sr " +
           "WHERE sr.advertisement.seller.id IN :sellerIds " +
           "GROUP BY sr.advertisement.seller.id")
    List<SellerRatingAggregate> findRatingAggregatesBySellerIds(@Param("sellerIds") List<Long> sellerIds);

    interface SellerRatingAggregate {
        Long getSellerId();
        Double getAverageRating();
        Long getRatingCount();
    }
}