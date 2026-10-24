package com.example.aikb.repository;

import com.example.aikb.entity.KnowledgeChunk;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    /**
     * 教学简化检索：MySQL LIKE 模糊匹配关键词，最多返回 5 条切片。
     * 正式项目可替换为 Embedding 向量化 + VectorStore 语义检索。
     */
    @Query("SELECT c FROM KnowledgeChunk c WHERE c.content LIKE %:keyword%")
    List<KnowledgeChunk> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
