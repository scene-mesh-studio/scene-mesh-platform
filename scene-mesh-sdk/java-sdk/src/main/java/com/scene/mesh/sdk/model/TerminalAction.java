package com.scene.mesh.sdk.model;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 终端动作（平台下发）
 */
@Data
public class TerminalAction {
    
    /**
     * 动作ID
     */
    private String id;
    
    /**
     * 动作类型
     */
    private String metaActionId;

    /**
     * 动作名称
     */
    private String name;

    /**
     * 终端ID
     */
    private String terminalId;
    
    /**
     * 下发时间戳
     */
    private Instant issueTimestamp;


    /**
     * 接收时间戳
     */
    private Instant receivedTimestamp;

    /**
     * 动作参数
     */
    private Map<String, Object> payload;
    
    // 构造函数
    public TerminalAction() {
        this.receivedTimestamp = Instant.now();
    }
    
    public TerminalAction(String metaActionId,String actionName, String terminalId, Instant issueTimestamp) {
        this();
        this.metaActionId = metaActionId;
        this.terminalId = terminalId;
        this.issueTimestamp = issueTimestamp;
        this.name = actionName;
    }
    
    public TerminalAction(String metaActionId,String actionName, String terminalId,Instant issueTimestamp, Map<String, Object> payload) {
        this(metaActionId, actionName, terminalId, issueTimestamp);
        this.payload = payload;
    }
}
