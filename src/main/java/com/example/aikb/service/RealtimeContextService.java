package com.example.aikb.service;

/**
 * 实时上下文服务：判断用户问题是否涉及实时信息（时间/天气），
 * 并生成注入到大模型 Prompt 的实时上下文文本。
 */
public interface RealtimeContextService {

    /**
     * 判断问题是否涉及实时信息（今天、日期、几点、天气、穿什么等）。
     */
    boolean isRealtimeQuestion(String message);

    /**
     * 生成实时上下文文本；非实时问题返回空字符串。
     * 实时问题返回类似：
     *   【实时信息】当前时间：2026年10月24日 星期六 18:35
     *   郑州：阴，气温 17°C，体感 16°C，湿度 60%，风速 12 km/h
     */
    String buildContext(String message);
}
