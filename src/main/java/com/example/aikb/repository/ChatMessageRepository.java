package com.example.aikb.repository;

import com.example.aikb.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** 按会话编号查询历史消息，按消息 ID 升序（即时间顺序） */
    List<ChatMessage> findByConversationIdOrderByIdAsc(String conversationId);
}
