package com.example.aikb.dto;

/** 前端提交的聊天请求体 */
public class ChatRequest {

    /** 用户问题 */
    private String message;

    /** 会话编号：首次聊天可为空，后端创建新会话后返回 */
    private String conversationId;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
