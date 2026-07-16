package com.example.secondhand.repository;

import com.example.secondhand.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByAdvertisementIdAndBuyerId(Long advertisementId, Long buyerId);

    List<Conversation> findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(Long buyerId, Long sellerId);
}