package com.scene.mesh.sdk.protocol;

import com.scene.mesh.sdk.model.TerminalConfig;
import com.scene.mesh.sdk.model.TerminalProtocolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 协议适配器 - 使用WebSocket
 */
@Slf4j
public class WebSocketProtocolAdapter implements ProtocolAdapter {
    
    private TerminalConfig config;
    private StandardWebSocketClient webSocketClient;
    private WebSocketSession webSocketSession;
    private MessageHandler messageHandler;
    private boolean connected = false;
    private CountDownLatch connectionLatch;
    
    @Override
    public void initialize(TerminalConfig config) {
        this.config = config;
        this.webSocketClient = new StandardWebSocketClient();
    }
    
    @Override
    public void connect(TerminalConfig config) {
        try {
            String wsUrl = buildWebSocketUrl(config);
            this.connectionLatch = new CountDownLatch(1);
            
            // 创建 WebSocket 处理器
            WebSocketHandler webSocketHandler = new AbstractWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    connected = true;
                    connectionLatch.countDown();
                }
                
                @Override
                public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    String text = message.getPayload();
                    if (messageHandler != null) {
                        messageHandler.onMessage(text);
                    } else {
                        log.warn("WebSocket 消息处理器为空: terminalId={}", config.getTerminalId());
                    }
                }
                
                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                    log.error("WebSocket 传输错误: terminalId={}", config.getTerminalId(), exception);
                    connected = false;
                }
                
                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                    connected = false;
                }
            };
            
            // 创建 WebSocket 握手头
            WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
            handshakeHeaders.add("productId", config.getProductId());
            handshakeHeaders.add("terminalId", config.getTerminalId());
            handshakeHeaders.add("secretKey", config.getSecretKey());
            
            // 建立连接
            webSocketSession = webSocketClient.execute(webSocketHandler, handshakeHeaders, URI.create(wsUrl)).get();
            
            // 等待连接建立
            if (!connectionLatch.await(config.getConnectTimeout(), TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("连接超时");
            }
            
        } catch (Exception e) {
            log.error("WebSocket 连接失败", e);
            connected = false;
            throw new RuntimeException("WebSocket 连接失败", e);
        }
    }
    
    @Override
    public void disconnect() {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
                connected = false;
            } catch (Exception e) {
                log.error("断开WebSocket 连接时出错", e);
            }
        }
    }
    
    @Override
    public void sendMessage(String message) {
        if (!isConnected()) {
            throw new IllegalStateException("WebSocket 客户端未连接");
        }

        try {
            webSocketSession.sendMessage(new TextMessage(message));
            log.debug("发送WebSocket 消息: {}", message);

        } catch (Exception e) {
            log.error("发送WebSocket 消息失败", e);
            throw new RuntimeException("发送WebSocket 消息失败", e);
        }
    }
    
    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }
    
    @Override
    public boolean isConnected() {
        return connected && webSocketSession != null && webSocketSession.isOpen();
    }
    
    @Override
    public String getProtocolType() {
        return TerminalProtocolType.WEBSOCKET.name();
    }
    
    /**
     * 构建 WebSocket URL - WebSocket
     */
    private String buildWebSocketUrl(TerminalConfig config) {
        String url = config.getServerUrl();
        if (url == null || url.isEmpty()) {
            url = "ws://localhost:8888";  // 使用正确的端口
        }
        
        // 添加 WebSocket 端点路径 - 与服务器端配置保持一致
        if (config.getProtocolVersion() != null) {
            url += "/ws/" + config.getProtocolVersion();
        } else {
            url += "/ws/v1";  // 与服务器端 WebSocketConfig 中的路径一致
        }
        
        return url;
    }
}
