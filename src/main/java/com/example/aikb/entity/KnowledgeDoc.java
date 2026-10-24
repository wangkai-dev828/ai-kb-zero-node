package com.example.aikb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 知识文档表：保存原始知识资料 */
@Entity
@Table(name = "knowledge_doc")
public class KnowledgeDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String title;

    /** 来源类型：text / file 等 */
    @Column(name = "source_type", length = 32)
    private String sourceType;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
