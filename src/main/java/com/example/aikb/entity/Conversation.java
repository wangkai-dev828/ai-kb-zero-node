package com.example.aikb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 会话表：一次多轮对话对应一条记录 */
@Entity
@Table(name = "conversation")
public class Conversation {

    /** 会话ID，使用 UUID，由后端生成后返回给前端 */
    @Id
    @Column(length = 64)
    private String id;

    /** 会话标题，通常取用户第一句话的前 20 字 */
    @Column(length = 255)
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
