package com.example.aikb.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Prompt 工程示例接口。
 *
 * GET /api/prompt/teacher?question=...  角色设定：Java 讲师
 * GET /api/prompt/format?topic=...      固定格式输出：概念/例子/代码/练习
 */
@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    private final ChatClient chatClient;

    public PromptController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** 角色设定：把模型设定为面向零基础学生的 Java 讲师 */
    @GetMapping("/teacher")
    public Map<String, String> teacher(@RequestParam String question) {
        String system = """
                你是一位经验丰富的 Java 讲师，授课对象是完全零基础的学生。
                要求：
                1. 语言通俗易懂，避免堆砌术语；
                2. 必须结合生活中的实例来解释概念；
                3. 语气温和耐心，像老师面对学生一样。
                """;
        String answer = chatClient.prompt()
                .system(system)
                .user(question)
                .call()
                .content();
        return Map.of("mode", "teacher", "answer", answer);
    }

    /** 固定格式输出：把 topic 嵌入任务模板，强制输出结构 */
    @GetMapping("/format")
    public Map<String, String> format(@RequestParam String topic) {
        String template = """
                请围绕主题「%s」进行讲解，输出必须严格包含以下四个部分，
                每部分使用对应的标题：
                一、概念：用 2~3 句话解释它是什么；
                二、例子：给出一个贴近生活的类比实例；
                三、代码：给出一段可运行的 Java 示例代码（带注释）；
                四、练习：给出一道供学生动手的小练习题。
                除这四个部分外不要输出任何其他内容。
                """.formatted(topic);
        String answer = chatClient.prompt()
                .user(template)
                .call()
                .content();
        return Map.of("mode", "format", "topic", topic, "answer", answer);
    }
}
