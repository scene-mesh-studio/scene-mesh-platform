package com.scene.mesh.sdk.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scene.mesh.sdk.model.TerminalAction;
import com.scene.mesh.sdk.model.TerminalEvent;
import com.scene.mesh.sdk.model.MessageType;
import lombok.Getter;

import java.util.Map;

/**
 * 消息工具类
 */
public class MessageUtils {

    /**
     * -- GETTER --
     *  获取 ObjectMapper 实例
     */
    @Getter
    private static final ObjectMapper objectMapper = createConfiguredObjectMapper();

    private static ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期时间序列化为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为指定类型的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为指定类型的对象（支持泛型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }
    
    /**
     * 将 JSON 字符串转换为 Map
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * 将 Map 转换为 JSON 字符串
     */
    public static String mapToJson(Map<String, Object> map) {
        return toJson(map);
    }
    
    /**
     * 序列化设备事件
     */
    public static String serializeEvent(TerminalEvent event) {
        return toJson(event);
    }
    
    /**
     * 反序列化设备事件
     */
    public static TerminalEvent deserializeEvent(String json) {
        return fromJson(json, TerminalEvent.class);
    }
    
    /**
     * 序列化设备动作
     */
    public static String serializeAction(TerminalAction action) {
        return toJson(action);
    }
    
    /**
     * 反序列化设备动作
     */
    public static TerminalAction deserializeAction(String json) {
        return fromJson(json, TerminalAction.class);
    }
    
    /**
     * 根据消息类型反序列化消息
     */
    public static Object deserializeMessage(String json, MessageType messageType) {
        switch (messageType) {
            case EVENT:
                return deserializeEvent(json);
            case ACTION:
                return deserializeAction(json);
            default:
                throw new IllegalArgumentException("不支持的消息类型: " + messageType);
        }
    }
    
    /**
     * 验证 JSON 字符串格式
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * 格式化 JSON 字符串（美化输出）
     */
    public static String formatJson(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 格式化失败", e);
        }
    }
}
