package com.example.aikb.controller;

import com.example.aikb.service.RealtimeContextService;
import org.springframework.http.MediaType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * SSE 流式输出接口。
 *
 * GET /api/chat/stream?message=...
 * produces = text/event-stream；返回 Flux<String>，
 * 内部通过 chatClient.prompt().user(message).stream().content()
 * 持续取得模型生成的文本片段并推送给浏览器。
 *
 * 实时问题（今天几号/天气/穿什么等）会自动注入实时上下文，
 * 与 /api/chat 的行为保持一致。
 */
@RestController
@RequestMapping("/api/chat")
public class SseController {

    private final ChatClient chatClient;
    private final RealtimeContextService realtimeContextService;

    public SseController(ChatClient chatClient, RealtimeContextService realtimeContextService) {
        this.chatClient = chatClient;
        this.realtimeContextService = realtimeContextService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        // 实时问题自动附加实时信息（时间/天气），普通问题原样发送
        String ctx = realtimeContextService.buildContext(message);
        String finalMessage = ctx.isEmpty()
                ? message
                : ctx + "\n用户问题：" + message + "\n请结合以上实时信息准确回答。";
        return chatClient.prompt()
                .user(finalMessage)
                .stream()
                .content()
                // 流末尾追加结束标记，前端收到 [DONE] 后主动关闭连接
                .concatWith(Flux.just("[DONE]"));
    }
}
