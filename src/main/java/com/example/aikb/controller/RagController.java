package com.example.aikb.controller;

import com.example.aikb.service.RagService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG 知识库接口。
 *
 * POST /api/rag/text?title=...&content=...   知识录入（入库 + 切分）
 * GET  /api/rag/ask?question=...             知识库问答
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/text")
    public Map<String, Object> save(@RequestParam String title,
                                    @RequestParam String content) {
        Long docId = ragService.saveKnowledge(title, content);
        return Map.of("docId", docId, "message", "知识保存成功");
    }

    @GetMapping("/ask")
    public Map<String, Object> ask(@RequestParam String question) {
        String answer = ragService.ask(question);
        return Map.of("question", question, "answer", answer);
    }
}
