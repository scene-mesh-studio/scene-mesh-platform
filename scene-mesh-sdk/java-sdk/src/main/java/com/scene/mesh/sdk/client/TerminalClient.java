package com.scene.mesh.sdk.client;

import com.scene.mesh.sdk.model.TerminalAction;
import com.scene.mesh.sdk.model.TerminalConfig;
import com.scene.mesh.sdk.model.TerminalEvent;
import com.scene.mesh.sdk.model.TerminalProtocolType;
import com.scene.mesh.sdk.protocol.MqttProtocolAdapter;
import com.scene.mesh.sdk.protocol.ProtocolAdapter;
import com.scene.mesh.sdk.protocol.WebSocketProtocolAdapter;
import com.scene.mesh.sdk.util.MessageUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 终端
 */
@Slf4j
public class TerminalClient {
    @Getter
    private final TerminalConfig config;
    private ProtocolAdapter protocolAdapter;
    private Consumer<TerminalAction> actionHandler;
    private boolean connected = false;

    public TerminalClient(TerminalConfig config) {
        this.config = config;
        initializeProtocolAdapter();
    }

    /**
     * 连接到服务器
     */
    public void connect() {
        if (connected) {
            log.warn("终端已连接，无需重复连接");
            return;
        }

        try {
            protocolAdapter.connect(config);
            connected = true;
        } catch (Exception e) {
            log.error("终端连接失败", e);
            connected = false;
            throw new RuntimeException("终端连接失败", e);
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (!connected) {
            log.warn("终端未连接，无需断开");
            return;
        }

        try {
            protocolAdapter.disconnect();
            connected = false;
        } catch (Exception e) {
            log.error("终端断开连接失败", e);
        }
    }

    /**
     * 发送设备事件
     */
    public CompletableFuture<Boolean> sendEvent(TerminalEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected()) {
                throw new IllegalStateException("终端未连接");
            }

            try {

                String message = MessageUtils.serializeEvent(event);
                protocolAdapter.sendMessage(message);

                return true;

            } catch (Exception e) {
                log.error("发送设备事件失败", e);
                return false;
            }
        });
    }

    /**
     * 设置动作处理器
     */
    public void onAction(Consumer<TerminalAction> handler) {
        this.actionHandler = handler;

        // 设置协议适配器的消息处理器
        protocolAdapter.setMessageHandler(message -> {
            try {
                TerminalAction action = MessageUtils.deserializeAction(message);
                if (actionHandler != null) {
                    actionHandler.accept(action);
                }
            } catch (Exception e) {
                log.error("处理接收到的动作失败: message={}", message, e);
            }
        });
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected && protocolAdapter != null && protocolAdapter.isConnected();
    }

    /**
     * 更新设备配置
     */
    public void updateConfig(TerminalConfig newConfig) {
        if (connected) {
            throw new IllegalStateException("设备已连接，无法更新配置");
        }

        this.config.setProtocolVersion(newConfig.getProtocolVersion());
        this.config.setServerUrl(newConfig.getServerUrl());
        this.config.setConnectTimeout(newConfig.getConnectTimeout());
        this.config.setHeartbeatInterval(newConfig.getHeartbeatInterval());
        this.config.setReconnectInterval(newConfig.getReconnectInterval());
        this.config.setMaxReconnectAttempts(newConfig.getMaxReconnectAttempts());
        this.config.setSecretKey(newConfig.getSecretKey());
        this.config.setProtocolType(newConfig.getProtocolType());
        this.config.setProductId(newConfig.getProductId());
        this.config.setTerminalId(newConfig.getTerminalId());

        // 重新初始化协议适配器
        initializeProtocolAdapter();

        log.info("设备配置已更新: terminalId={}", config.getTerminalId());
    }

    /**
     * 获取协议类型
     */
    public TerminalProtocolType getProtocolType() {
        return config.getProtocolType();
    }

    /**
     * 初始化协议适配器
     */
    private void initializeProtocolAdapter() {
        switch (config.getProtocolType()) {
            case MQTT:
                protocolAdapter = new MqttProtocolAdapter();
                break;
            case WEBSOCKET:
                protocolAdapter = new WebSocketProtocolAdapter();
                break;
            default:
                throw new IllegalArgumentException("不支持的协议类型: " + config.getProtocolType());
        }

        protocolAdapter.initialize(config);
    }
}
