package com.example.aikb.service;

import com.example.aikb.dto.ChatResponse;
import com.example.aikb.entity.ChatMessage;

import java.util.List;

/** 聊天业务接口 */
public interface ChatService {

    /** 普通聊天：持久化会话与消息，返回会话编号和 AI 回答 */
    ChatResponse chat(String message, String conversationId);

    /** 查询某会话的全部历史消息 */
    List<ChatMessage> history(String conversationId);
}
