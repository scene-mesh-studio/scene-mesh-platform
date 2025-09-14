package com.scene.mesh.sdk.protocol;

import com.scene.mesh.sdk.model.TerminalConfig;
import java.util.Map;

/**
 * 协议适配器接口
 */
public interface ProtocolAdapter {
    
    /**
     * 初始化协议适配器
     * @param config 终端配置
     */
    void initialize(TerminalConfig config);
    
    /**
     * 连接到服务器
     */
    void connect(TerminalConfig config);
    
    /**
     * 断开连接
     */
    void disconnect();
    
    /**
     * 发送消息
     * @param message 消息内容
     */
    void sendMessage(String message);

    /**
     * 设置消息接收处理器
     * @param handler 消息处理器
     */
    void setMessageHandler(MessageHandler handler);
    
    /**
     * 检查连接状态
     * @return 是否已连接
     */
    boolean isConnected();
    
    /**
     * 获取协议类型
     * @return 协议类型
     */
    String getProtocolType();
    
    /**
     * 消息处理器接口
     */
    @FunctionalInterface
    interface MessageHandler {
        /**
         * 处理接收到的消息
         * @param message 消息内容
         */
        void onMessage(String message);
    }
}
