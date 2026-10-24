package com.example.aikb.controller;

import com.example.aikb.dto.ChatRequest;
import com.example.aikb.dto.ChatResponse;
import com.example.aikb.entity.ChatMessage;
import com.example.aikb.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 基础 AI 聊天接口。
 * POST /api/chat      普通聊天（一次性返回完整回答）
 * GET  /api/chat/history?conversationId=...  查询会话历史
 * GET  /api/health    健康检查
 */
@RestController
@RequestMapping("/api")
public class AiChatController {

    private final ChatService chatService;

    public AiChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
        return chatService.chat(request.getMessage(), request.getConversationId());
    }

    @GetMapping("/chat/history")
    public List<ChatMessage> history(@RequestParam String conversationId) {
        return chatService.history(conversationId);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "time", System.currentTimeMillis());
    }
}
