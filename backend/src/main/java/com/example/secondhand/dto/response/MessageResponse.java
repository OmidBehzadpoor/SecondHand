package com.example.secondhand.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private Long senderId;
    private String senderUsername;
    private LocalDateTime createdAt;
    private boolean isRead;
}