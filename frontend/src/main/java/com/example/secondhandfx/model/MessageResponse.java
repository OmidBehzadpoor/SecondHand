package com.example.secondhandfx.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private String content;
    private Long senderId;
    private String senderUsername;
    private LocalDateTime createdAt;
    private boolean isRead;
}