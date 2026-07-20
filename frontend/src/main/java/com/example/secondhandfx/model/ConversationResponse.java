package com.example.secondhandfx.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {
    private Long id;
    private Long advertisementId;
    private String advertisementTitle;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private String lastMessage;
    private int unreadCount;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}