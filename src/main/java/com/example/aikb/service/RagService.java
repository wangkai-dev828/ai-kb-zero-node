package com.example.aikb.service;

/** RAG 知识库业务接口 */
public interface RagService {

    /** 保存知识资料：入库 + 切分 + 切片入库，返回文档ID */
    Long saveKnowledge(String title, String content);

    /** 知识库问答：检索 -> 拼接上下文 -> 增强回答 */
    String ask(String question);
}
