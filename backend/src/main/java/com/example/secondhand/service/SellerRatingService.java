package com.example.secondhand.service;

import com.example.secondhand.dto.SellerRatingRequest;
import com.example.secondhand.dto.SellerRatingSummary;
import com.example.secondhand.dto.response.SellerRatingResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.RatingAlreadyExistsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.SellerRating;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.SellerRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerRatingService {

    private final SellerRatingRepository sellerRatingRepository;
    private final AdvertisementRepository advertisementRepository;

    @Transactional
    public SellerRatingResponse rateAdvertisement(Long advertisementId, SellerRatingRequest request, User currentUser) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED
                && advertisement.getStatus() != AdvertisementStatus.SOLD) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        if (advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما نمی‌توانید به آگهی خودتان امتیاز دهید");
        }

        if (sellerRatingRepository.existsByBuyerIdAndAdvertisementId(currentUser.getId(), advertisementId)) {
            throw new RatingAlreadyExistsException("شما قبلاً به این آگهی امتیاز داده‌اید");
        }

        SellerRating sellerRating = SellerRating.builder().buyer(currentUser)
                .advertisement(advertisement).rating(request.getRating()).comment(request.getComment()).build();

        return mapToResponse(sellerRatingRepository.save(sellerRating));

    }

    @Transactional(readOnly = true)
    public List<SellerRatingResponse> getSellerRatings(Long sellerId) {
        return sellerRatingRepository.findByAdvertisementSellerId(sellerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getSellerRatingCount(Long sellerId) {
        return (long) sellerRatingRepository.findByAdvertisementSellerId(sellerId).size();
    }

    @Transactional(readOnly = true)
    public Double getSellerAverageRating(Long sellerId) {

        List<SellerRating> ratings = sellerRatingRepository.findByAdvertisementSellerId(sellerId);
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream()
                .mapToInt(SellerRating::getRating)
                .average()
                .orElse(0.0);

    }


    @Transactional(readOnly = true)
    public Map<Long, SellerRatingSummary> getRatingSummariesForSellers(List<Long> sellerIds) {
        Map<Long, SellerRatingSummary> summaries = new HashMap<>();

        if (sellerIds == null || sellerIds.isEmpty()) {
            return summaries;
        }

        List<Long> distinctSellerIds = sellerIds.stream().distinct().toList();

        for (Long sellerId : distinctSellerIds) {
            summaries.put(sellerId, SellerRatingSummary.EMPTY);
        }

        sellerRatingRepository.findRatingAggregatesBySellerIds(distinctSellerIds)
                .forEach(aggregate -> summaries.put(
                        aggregate.getSellerId(),
                        new SellerRatingSummary(aggregate.getAverageRating(), aggregate.getRatingCount())
                ));

        return summaries;
    }

    private SellerRatingResponse mapToResponse(SellerRating sellerRating) {
        return SellerRatingResponse.builder()
                .id(sellerRating.getId())
                .buyerId(sellerRating.getBuyer().getId())
                .buyerUsername(sellerRating.getBuyer().getUsername())
                .sellerId(sellerRating.getAdvertisement().getSeller().getId())
                .sellerUsername(sellerRating.getAdvertisement().getSeller().getUsername())
                .advertisementId(sellerRating.getAdvertisement().getId())
                .advertisementTitle(sellerRating.getAdvertisement().getTitle())
                .rating(sellerRating.getRating())
                .comment(sellerRating.getComment())
                .createdAt(sellerRating.getCreatedAt())
                .build();
    }

}