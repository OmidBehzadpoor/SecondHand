package com.example.secondhand.repository;

import com.example.secondhand.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    long countByConversationIdAndIsReadFalseAndSenderIdNot(Long conversationId, Long senderId);
}
