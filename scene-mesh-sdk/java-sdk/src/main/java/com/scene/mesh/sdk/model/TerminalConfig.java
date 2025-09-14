package com.scene.mesh.sdk.model;

import lombok.Data;

/**
 * 终端配置
 */
@Data
public class TerminalConfig {
    
    /**
     * 协议类型
     */
    private TerminalProtocolType protocolType;

    /**
     * 协议版本
     */
    private String protocolVersion;

    /**
     * 服务器地址
     */
    private String serverUrl;
    
    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 30000;
    
    /**
     * 心跳间隔（毫秒）
     */
    private Integer heartbeatInterval = 60000;
    
    /**
     * 重连间隔（毫秒）
     */
    private Integer reconnectInterval = 5000;
    
    /**
     * 最大重连次数
     */
    private Integer maxReconnectAttempts = 5;

    /**
     * 产品ID
     */
    private String productId;

    /**
     * 终端 ID
     */
    private String terminalId;

    /**
     * 密钥
     */
    private String secretKey;

    // 构造函数
    public TerminalConfig() {}

    public TerminalConfig(String productId, String terminalId, String secretKey, TerminalProtocolType protocolType) {
        this.productId = productId;
        this.terminalId = terminalId;
        this.protocolType = protocolType;
        this.secretKey = secretKey;
    }
}
