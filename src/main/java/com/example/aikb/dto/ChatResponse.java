package com.example.aikb.dto;

/** 聊天响应体：返回会话编号和 AI 回答 */
public class ChatResponse {

    private String conversationId;
    private String answer;

    public ChatResponse() {}

    public ChatResponse(String conversationId, String answer) {
        this.conversationId = conversationId;
        this.answer = answer;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
