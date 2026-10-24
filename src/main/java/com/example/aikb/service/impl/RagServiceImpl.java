package com.example.aikb.service.impl;

import com.example.aikb.entity.KnowledgeChunk;
import com.example.aikb.entity.KnowledgeDoc;
import com.example.aikb.repository.KnowledgeChunkRepository;
import com.example.aikb.repository.KnowledgeDocRepository;
import com.example.aikb.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG 知识库业务实现（教学简化版）。
 *
 * 完整链路：保存知识资料 -> 文本切分 -> 切片入库
 *          -> 根据问题检索相关切片 -> 拼接上下文
 *          -> 将「上下文+问题」提交大模型 -> 返回增强回答。
 *
 * 检索采用 MySQL LIKE 模糊匹配（限制 5 条）；
 * 正式项目可替换为 Embedding 向量化 + VectorStore 语义检索。
 */
@Service
public class RagServiceImpl implements RagService {

    /** 简化切分规则：每 500 字一段 */
    private static final int CHUNK_SIZE = 500;
    /** 检索返回的最大切片数 */
    private static final int TOP_N = 5;
    /** 中文常见疑问词/虚词，检索前剔除，避免 LIKE 匹配失败 */
    private static final String[] STOP_WORDS = {
            "什么", "怎么", "怎样", "如何", "为什么", "请问", "一下",
            "多少", "哪些", "还是", "可以", "吗", "呢", "啊", "的", "了", "？", "?"
    };

    private final ChatClient chatClient;
    private final KnowledgeDocRepository docRepository;
    private final KnowledgeChunkRepository chunkRepository;

    public RagServiceImpl(ChatClient chatClient,
                          KnowledgeDocRepository docRepository,
                          KnowledgeChunkRepository chunkRepository) {
        this.chatClient = chatClient;
        this.docRepository = docRepository;
        this.chunkRepository = chunkRepository;
    }

    @Override
    @Transactional
    public Long saveKnowledge(String title, String content) {
        // 1. 原始资料写入 knowledge_doc
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(title);
        doc.setSourceType("text");
        doc.setContent(content);
        doc = docRepository.save(doc);

        // 2. 按 500 字/段切分，顺序写入 knowledge_chunk
        List<KnowledgeChunk> chunks = new ArrayList<>();
        int index = 0;
        for (int start = 0; start < content.length(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, content.length());
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocId(doc.getId());
            chunk.setChunkIndex(index++);
            chunk.setContent(content.substring(start, end));
            chunks.add(chunk);
        }
        chunkRepository.saveAll(chunks);
        return doc.getId();
    }

    @Override
    public String ask(String question) {
        // 1. 提取关键词（去除疑问词等停用词）
        String keyword = extractKeyword(question);

        // 2. LIKE 模糊检索，最多 5 条；整句匹配不到时逐级缩短关键词重试
        //    （例如「公司年假有几天」→「公司年假」，避免整句 LIKE 匹配失败）
        Pageable pageable = PageRequest.of(0, TOP_N);
        List<KnowledgeChunk> hits = chunkRepository.searchByKeyword(keyword, pageable);
        for (int len = keyword.length() - 1; hits.isEmpty() && len >= 2; len--) {
            hits = chunkRepository.searchByKeyword(keyword.substring(0, len), pageable);
        }

        // 3. 拼接上下文，构造增强 Prompt
        StringBuilder context = new StringBuilder();
        for (KnowledgeChunk c : hits) {
            context.append("【资料片段").append(c.getChunkIndex() + 1).append("】")
                   .append(c.getContent()).append("\n\n");
        }

        String prompt;
        if (context.isEmpty()) {
            prompt = "用户问题：" + question
                    + "\n\n知识库中没有检索到相关资料，请如实告知用户暂无相关知识。";
        } else {
            prompt = "你是一个企业知识库问答助手。请严格依据以下资料回答用户问题，"
                    + "不要使用资料之外的知识，不要编造。\n\n"
                    + "【参考资料】\n" + context
                    + "【用户问题】\n" + question
                    + "\n\n请基于上述资料回答：";
        }

        // 4. 提交大模型，返回增强回答
        return chatClient.prompt().user(prompt).call().content();
    }

    /** 去掉疑问词/虚词后返回剩余关键词，供 LIKE 匹配使用 */
    private String extractKeyword(String question) {
        String kw = question;
        for (String w : STOP_WORDS) {
            kw = kw.replace(w, "");
        }
        kw = kw.trim();
        return kw.isEmpty() ? question : kw;
    }
}
