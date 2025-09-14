package com.scene.mesh.sdk.client;

import com.scene.mesh.sdk.model.TerminalConfig;
import com.scene.mesh.sdk.model.TerminalProtocolType;

/**
 * 终端客户端构建器
 */
public class TerminalClientBuilder {
    
    private TerminalConfig config;
    
    private TerminalClientBuilder() {
        this.config = new TerminalConfig();
    }
    
    /**
     * 创建构建器实例
     */
    public static TerminalClientBuilder builder() {
        return new TerminalClientBuilder();
    }
    
    /**
     * 设置产品ID
     */
    public TerminalClientBuilder productId(String productId) {
        config.setProductId(productId);
        return this;
    }
    
    /**
     * 设置设备ID
     */
    public TerminalClientBuilder terminalId(String terminalId) {
        config.setTerminalId(terminalId);
        return this;
    }
    
    /**
     * 设置协议类型
     */
    public TerminalClientBuilder protocol(TerminalProtocolType protocolType) {
        config.setProtocolType(protocolType);
        return this;
    }
    
    /**
     * 设置服务器地址
     */
    public TerminalClientBuilder serverUrl(String serverUrl) {
        config.setServerUrl(serverUrl);
        return this;
    }
    
    /**
     * 设置连接超时时间
     */
    public TerminalClientBuilder connectTimeout(Integer connectTimeout) {
        config.setConnectTimeout(connectTimeout);
        return this;
    }
    
    /**
     * 设置心跳间隔
     */
    public TerminalClientBuilder heartbeatInterval(Integer heartbeatInterval) {
        config.setHeartbeatInterval(heartbeatInterval);
        return this;
    }
    
    /**
     * 设置重连间隔
     */
    public TerminalClientBuilder reconnectInterval(Integer reconnectInterval) {
        config.setReconnectInterval(reconnectInterval);
        return this;
    }
    
    /**
     * 设置最大重连次数
     */
    public TerminalClientBuilder maxReconnectAttempts(Integer maxReconnectAttempts) {
        config.setMaxReconnectAttempts(maxReconnectAttempts);
        return this;
    }

    /**
     * 设置密钥
     */
    public TerminalClientBuilder secretKey(String secretKey){
        config.setSecretKey(secretKey);
        return this;
    }

    /**
     * 设置协议版本
     */
    public TerminalClientBuilder protocolVersion(String protocolVersion) {
        config.setProtocolVersion(protocolVersion);
        return this;
    }

    /**
     * 设置完整配置
     */
    public TerminalClientBuilder config(TerminalConfig config) {
        this.config = config;
        return this;
    }
    
    /**
     * 构建设备客户端
     */
    public TerminalClient build() {
        validateConfig();
        return new TerminalClient(config);
    }
    
    /**
     * 验证配置
     */
    private void validateConfig() {
        if (config.getProductId() == null || config.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("产品ID不能为空");
        }
        if (config.getTerminalId() == null || config.getTerminalId().trim().isEmpty()) {
            throw new IllegalArgumentException("终端ID不能为空");
        }
        if (config.getProtocolType() == null) {
            throw new IllegalArgumentException("协议类型不能为空");
        }
    }
}
