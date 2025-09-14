package com.scene.mesh.sdk.model;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 终端事件（终端上报）
 */
@Data
public class TerminalEvent {
    
    /**
     * 事件ID
     */
    private String id;
    
    /**
     * 事件类型
     */
    private String type;
    
    /**
     * 发生时间戳
     */
    private Instant occurredAt;
    
    /**
     * 事件数据
     */
    private Map<String, Object>  payload;
    
    // 构造函数
    public TerminalEvent() {
        this.occurredAt = Instant.now();
        this.id = UUID.randomUUID().toString();
    }
    
    public TerminalEvent(String type) {
        this();
        this.type = type;
    }
    
    public TerminalEvent(String type, Map<String, Object> payload) {
        this(type);
        this.payload = payload;
    }
}