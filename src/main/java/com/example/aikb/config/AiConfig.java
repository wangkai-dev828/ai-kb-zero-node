package com.example.aikb.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置类：通过 ChatClient.Builder 创建全局统一的 ChatClient。
 *
 * 系统角色约束设定为「企业级AI知识库助手」，所有 Controller / Service
 * 直接注入该 Bean 即可调用大模型，无需重复构建。
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是「企业级AI知识库助手」，回答要求：专业、简洁、条理清晰；" +
                        "涉及企业内部知识时，必须依据提供的资料回答，不要编造。")
                .build();
    }
}
