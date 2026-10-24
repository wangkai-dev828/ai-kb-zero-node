-- =====================================================
-- AI 大模型知识库问答系统 · 数据库初始化脚本
-- 使用方式：mysql -uroot -p < schema.sql
-- =====================================================

CREATE DATABASE IF NOT EXISTS ai_kb_zero_node
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_kb_zero_node;

-- 1. 会话表
CREATE TABLE IF NOT EXISTS conversation (
    id          VARCHAR(64)  NOT NULL COMMENT '会话ID（UUID）',
    title       VARCHAR(255) NULL     COMMENT '会话标题（取用户第一句话）',
    created_at  DATETIME     NULL,
    updated_at  DATETIME     NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 2. 聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    conversation_id VARCHAR(64)  NOT NULL COMMENT '所属会话ID',
    role            VARCHAR(16)  NOT NULL COMMENT 'user / assistant / system',
    content         TEXT         NOT NULL COMMENT '消息内容',
    created_at      DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_cm_conversation (conversation_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 3. 知识文档表
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255) NULL COMMENT '文档标题',
    source_type VARCHAR(32)  NULL COMMENT '来源类型：text / file 等',
    content     LONGTEXT     NULL COMMENT '原始内容',
    created_at  DATETIME     NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 4. 知识切片表
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    doc_id      BIGINT   NOT NULL COMMENT '所属文档ID',
    chunk_index INT      NOT NULL COMMENT '切片序号（从0开始）',
    content     TEXT     NOT NULL COMMENT '切片文本',
    created_at  DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_kc_doc (doc_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
