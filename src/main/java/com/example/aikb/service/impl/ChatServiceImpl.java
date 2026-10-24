package com.example.aikb.service;

import com.example.aikb.dto.ChatResponse;
import com.example.aikb.entity.ChatMessage;
import com.example.aikb.entity.Conversation;
import com.example.aikb.repository.ChatMessageRepository;
import com.example.aikb.repository.ConversationRepository;
import com.example.aikb.service.RealtimeContextService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 聊天业务实现：会话管理 + 消息持久化 + 大模型调用。
 *
 * 流程：根据 conversationId 查会话 -> 没有则新建（标题取用户第一句话）
 *      -> 保存 role=user 的用户消息 -> 调用大模型
 *      -> 保存 role=assistant 的 AI 消息 -> 返回会话编号和回答。
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RealtimeContextService realtimeContextService;

    public ChatServiceImpl(ChatClient chatClient,
                           ConversationRepository conversationRepository,
                           ChatMessageRepository chatMessageRepository,
                           RealtimeContextService realtimeContextService) {
        this.chatClient = chatClient;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.realtimeContextService = realtimeContextService;
    }

    @Override
    @Transactional
    public ChatResponse chat(String message, String conversationId) {
        // 1. 会话管理：没有会话编号就创建新会话
        Conversation conversation;
        if (conversationId == null || conversationId.isBlank()) {
            conversation = new Conversation();
            conversation.setId(UUID.randomUUID().toString());
            // 标题取用户第一句话的前 20 字
            String title = message.length() > 20 ? message.substring(0, 20) : message;
            conversation.setTitle(title);
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("会话不存在: " + conversationId));
        }

        // 2. 先保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversationId(conversation.getId());
        userMsg.setRole("user");
        userMsg.setContent(message);
        chatMessageRepository.save(userMsg);

        // 3. 调用大模型（多轮场景把历史记录一并带上）
        List<ChatMessage> history =
                chatMessageRepository.findByConversationIdOrderByIdAsc(conversation.getId());
        String answer;
        if (history.size() <= 1) {
            // 首轮：直接问（实时问题自动附加实时信息）
            answer = chatClient.prompt().user(withRealtime(message)).call().content();
        } else {
            // 多轮：拼接上下文
            StringBuilder ctx = new StringBuilder();
            for (ChatMessage m : history) {
                ctx.append(m.getRole().equals("user") ? "用户：" : "助手：")
                   .append(m.getContent()).append("\n");
            }
            String prompt = "以下是当前会话的历史记录，请结合上下文回答最后一条用户消息：\n"
                    + ctx + "\n请回答：" + withRealtime(message);
            answer = chatClient.prompt().user(prompt).call().content();
        }

        // 4. 保存 AI 消息
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setConversationId(conversation.getId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(answer);
        chatMessageRepository.save(aiMsg);

        // 5. 更新会话并返回
        conversationRepository.save(conversation);
        return new ChatResponse(conversation.getId(), answer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> history(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByIdAsc(conversationId);
    }

    /**
     * 实时问题（今天几号/天气/穿什么等）自动在用户消息前注入实时上下文，
     * 让模型基于真实数据回答，避免编造。
     */
    private String withRealtime(String message) {
        String ctx = realtimeContextService.buildContext(message);
        if (ctx.isEmpty()) {
            return message;
        }
        return ctx + "\n用户问题：" + message + "\n请结合以上实时信息准确回答。";
    }
}
