package com.example.secondhand.repository;

import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByStatus(AdvertisementStatus status);

    List<Advertisement> findBySellerId(Long sellerId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByCityId(Long cityId);

    @Query("""
    SELECT a FROM Advertisement a
    WHERE a.status = :status
      AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:categoryId IS NULL OR a.category.id = :categoryId)
      AND (:cityId IS NULL OR a.city.id = :cityId)
      AND (:minPrice IS NULL OR a.price >= :minPrice)
      AND (:maxPrice IS NULL OR a.price <= :maxPrice)
    ORDER BY
      CASE WHEN :sortBy = 'PRICE_ASC' THEN a.price END ASC,
      CASE WHEN :sortBy = 'PRICE_DESC' THEN a.price END DESC,
      CASE WHEN :sortBy = 'OLDEST' THEN a.createdAt END ASC,
      a.createdAt DESC
    """)
    List<Advertisement> search(
            @Param("status") AdvertisementStatus status,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("cityId") Long cityId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("sortBy") String sortBy
    );
}
