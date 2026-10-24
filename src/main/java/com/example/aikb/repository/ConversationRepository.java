package com.example.aikb.repository;

import com.example.aikb.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
}
