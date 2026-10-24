package com.example.aikb.service.impl;

import com.example.aikb.service.RealtimeContextService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实时上下文服务实现。
 *
 * 原理：检测到问题涉及时间/天气等实时信息时，在后端拼装一段
 * 「实时信息」文本，附加到用户问题之前，让大模型基于真实数据回答，
 * 避免模型凭空编造"今天的天气"。
 *
 * 天气数据来源：Open-Meteo 免费接口（无需 API Key）：
 *   1) geocoding 接口：城市名 -> 经纬度
 *   2) forecast 接口：经纬度 -> 当前天气
 * 同一城市天气结果缓存 10 分钟，避免频繁外呼。
 */
@Service
public class RealtimeContextServiceImpl implements RealtimeContextService {

    /** 触发实时上下文的关键词：命中任意一个即注入实时信息 */
    private static final List<String> KEYWORDS = List.of(
            "今天", "今日", "现在", "当前", "几号", "几日", "日期", "星期几", "几点",
            "时间", "天气", "气温", "温度", "下雨", "降水", "湿度", "风速",
            "穿什么", "带伞", "冷不冷", "热不热", "适合出门", "紫外线");

    /** 常见城市 -> 中文城市名（用于从问题中提取目标城市） */
    private static final List<String> CITIES = List.of(
            "郑州", "北京", "上海", "广州", "深圳", "杭州", "武汉", "西安",
            "成都", "重庆", "南京", "天津", "苏州", "长沙", "新乡", "洛阳", "开封");

    private static final String DEFAULT_CITY = "郑州";
    private static final long CACHE_MS = 10 * 60 * 1000; // 天气缓存 10 分钟

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    /** 天气缓存：城市 -> [时间戳, 天气文本] */
    private final Map<String, Object[]> weatherCache = new ConcurrentHashMap<>();

    @Override
    public boolean isRealtimeQuestion(String message) {
        if (message == null) return false;
        return KEYWORDS.stream().anyMatch(message::contains);
    }

    @Override
    public String buildContext(String message) {
        if (!isRealtimeQuestion(message)) return "";

        StringBuilder sb = new StringBuilder("【实时信息】\n");
        LocalDateTime now = LocalDateTime.now();
        String dateLine = now.format(DateTimeFormatter.ofPattern(
                "yyyy年M月d日 EEEE HH:mm")) .replace("星期", "星期");
        sb.append("当前时间：").append(toChineseWeek(dateLine)).append("\n");

        String weather = getWeather(extractCity(message));
        if (weather != null) {
            sb.append(weather);
        }
        return sb.toString();
    }

    /** 从问题中提取城市名；没有明确城市则用默认城市 */
    private String extractCity(String message) {
        return CITIES.stream().filter(message::contains).findFirst().orElse(DEFAULT_CITY);
    }

    /** Open-Meteo 返回的星期是英文，统一换成中文星期X */
    private String toChineseWeek(String dateLine) {
        return dateLine
                .replace("Monday", "一").replace("Tuesday", "二")
                .replace("Wednesday", "三").replace("Thursday", "四")
                .replace("Friday", "五").replace("Saturday", "六")
                .replace("Sunday", "日");
    }

    /** 取某城市当前天气文本（带 10 分钟缓存），失败返回 null（此时上下文只有时间） */
    private String getWeather(String city) {
        Object[] cached = weatherCache.get(city);
        long now = System.currentTimeMillis();
        if (cached != null && now - (long) cached[0] < CACHE_MS) {
            return (String) cached[1];
        }
        try {
            // 1) 城市名 -> 经纬度
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + URLEncoder.encode(city, StandardCharsets.UTF_8)
                    + "&count=1&language=zh&format=json";
            JsonNode geo = mapper.readTree(httpGet(geoUrl));
            JsonNode hit = geo.path("results").path(0);
            if (hit.isMissingNode()) return null;
            double lat = hit.get("latitude").asDouble();
            double lon = hit.get("longitude").asDouble();

            // 2) 经纬度 -> 当前天气
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                    + "&longitude=" + lon
                    + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto";
            JsonNode cur = mapper.readTree(httpGet(url)).path("current");

            String text = city + "当前天气：" + wmoText(cur.get("weather_code").asInt())
                    + "，气温 " + cur.get("temperature_2m").asInt() + "°C"
                    + "，体感 " + cur.get("apparent_temperature").asInt() + "°C"
                    + "，湿度 " + cur.get("relative_humidity_2m").asInt() + "%"
                    + "，风速 " + cur.get("wind_speed_10m").asInt() + " km/h\n";
            weatherCache.put(city, new Object[]{now, text});
            return text;
        } catch (Exception e) {
            return null; // 天气获取失败不影响回答，只注入时间信息
        }
    }

    private String httpGet(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8)).GET().build();
        return http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
    }

    /** WMO 天气代码 -> 中文描述 */
    private String wmoText(int code) {
        return switch (code) {
            case 0 -> "晴";
            case 1, 2 -> "多云";
            case 3 -> "阴";
            case 45, 48 -> "雾";
            case 51, 53, 55, 56, 57 -> "毛毛雨";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "雨";
            case 71, 73, 75, 77, 85, 86 -> "雪";
            case 95, 96, 99 -> "雷雨";
            default -> "未知(" + code + ")";
        };
    }
}
