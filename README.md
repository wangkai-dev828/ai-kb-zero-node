# AI 大模型知识库问答系统（ai-kb-zero-node）

> 企业级 AI 聊天机器人实战项目 —— 基于 SpringBoot 3 + Spring AI 接入 DeepSeek 大模型，实现基础聊天、SSE 流式输出、Prompt 工程与 RAG 知识库问答，并扩展了实时信息（天气/日期）问答能力。

## ✨ 功能特性

| 模块 | 说明 |
|---|---|
| 💬 基础聊天 | `/api/chat` 多轮对话，历史上下文 + 会话/消息全部持久化到 MySQL |
| ⚡ SSE 流式输出 | `/api/chat/stream` 打字机逐字输出，`[DONE]` 结束标记 |
| 🎯 Prompt 工程 | 角色设定（Java 讲师）+ 固定格式输出（一/二/三/四 结构） |
| 📚 RAG 知识库 | 文本录入 → 500 字切分 → LIKE 检索（去停用词 + 整句匹配失败自动缩短关键词重试）→ 增强约束 Prompt，知识外问题不编造 |
| 🌤 实时信息问答 | 问"今天几号/天气/穿什么"自动注入实时上下文（本地时间 + Open-Meteo 免费天气，城市可识别、10 分钟缓存） |

## 🛠 技术栈

- **后端**：Java 17、SpringBoot 3.3.5、Spring AI 1.0.0 GA（DeepSeek OpenAI 兼容接入）、Spring Data JPA、Reactor（Flux 流式）
- **数据库**：MySQL 8（4 张表：conversation / chat_message / knowledge_doc / knowledge_chunk）
- **前端**：Vue 3 CDN 直连，**零 Node.js 环境**，5 个静态页面（首页/聊天/SSE/Prompt/RAG）+ 全站实时信息栏
- **外部服务**：DeepSeek API（对话）、Open-Meteo（天气，无需 Key）

## 🚀 快速开始

### 1. 环境准备

- JDK 17+、Maven 3.9+、MySQL 8
- DeepSeek API Key：[platform.deepseek.com](https://platform.deepseek.com/) 注册获取

### 2. 配置

设置环境变量 `DEEPSEEK_API_KEY`（Key 不写入代码），并按需修改 `application.yml` 中的数据库账号密码。

### 3. 建库

```bash
mysql -uroot -p < src/main/resources/schema.sql
```

### 4. 启动

```bash
mvn spring-boot:run
```

访问 <http://localhost:8080> 即可体验全部页面。

## 📡 接口一览

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/health` | GET | 健康检查 |
| `/api/chat` | POST | 聊天（多轮，body: `{message, conversationId}`） |
| `/api/chat/history` | GET | 会话历史 |
| `/api/chat/stream` | GET(SSE) | 流式聊天 |
| `/api/prompt/teacher` | GET | Java 讲师角色问答 |
| `/api/prompt/format` | GET | 固定格式输出 |
| `/api/rag/text` | POST | 知识文本录入（自动切分） |
| `/api/rag/ask` | GET | 知识库问答 |

## 📖 学习路线

详见《[复现指南.md](复现指南.md)》——按阶段 0-9 从环境安装到整合验收，每阶段附验证步骤与常见问题速查表，末尾含实时问答进阶扩展。
