package com.scene.mesh.sdk.protocol;

import com.scene.mesh.sdk.model.TerminalConfig;
import com.scene.mesh.sdk.model.TerminalProtocolType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT 协议适配器
 */
@Slf4j
public class MqttProtocolAdapter implements ProtocolAdapter {
    
    private TerminalConfig config;
    private MqttClient mqttClient;
    private MessageHandler messageHandler;
    private boolean connected = false;
    
    @Override
    public void initialize(TerminalConfig config) {
        this.config = config;
        try {
            // 创建 MQTT 客户端
            String clientId = config.getTerminalId();
            String brokerUrl = buildBrokerUrl(config);
            
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            
            // 设置连接选项
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 连接丢失", cause);
                    connected = false;
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    log.debug("接收到 MQTT 消息: topic={}, payload={}", topic, payload);

                    //处理 error
                    if (topic.equals(getErrorTopic(clientId))) {
                        throw new RuntimeException("Error topic - "+ message);
                    }

                    //处理 action
                    if (topic.equals(getActionTopic(clientId))) {
                        if (messageHandler != null) {
                            messageHandler.onMessage(payload);
                        }
                    }

                    //处理其他

                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.debug("MQTT 消息发送完成");
                }
            });
            
            log.info("MQTT 协议适配器初始化完成: brokerUrl={}, clientId={}", brokerUrl, clientId);
            
        } catch (MqttException e) {
            log.error("MQTT 协议适配器初始化失败", e);
            throw new RuntimeException("MQTT 协议适配器初始化失败", e);
        }
    }
    
    @Override
    public void connect(TerminalConfig config) {
        if (mqttClient == null) {
            throw new IllegalStateException("MQTT 客户端未初始化");
        }
        
        try {
            if (!mqttClient.isConnected()) {
                MqttConnectOptions options = createConnectOptions(config);
                mqttClient.connect(options);
                
                // 订阅动作主题
                String actionTopic = getActionTopic(config.getTerminalId());
                mqttClient.subscribe(actionTopic, 1);
                
                connected = true;
                log.info("MQTT 连接成功: terminalId={}", config.getTerminalId());
            }
        } catch (MqttException e) {
            log.error("MQTT 连接失败", e);
            connected = false;
            throw new RuntimeException("MQTT 连接失败", e);
        }
    }
    
    @Override
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                connected = false;
                log.info("MQTT 连接已断开");
            } catch (MqttException e) {
                log.error("MQTT 断开连接失败", e);
            }
        }
    }
    
    @Override
    public void sendMessage(String message) {
        if (!isConnected()) {
            throw new IllegalStateException("MQTT 客户端未连接");
        }

        try {
            String eventTopic = getEventTopic(config.getTerminalId());
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1);
            mqttMessage.setRetained(false);

            mqttClient.publish(eventTopic, mqttMessage);
            log.debug("发送 MQTT 消息: topic={}, message={}", eventTopic, message);

        } catch (MqttException e) {
            log.error("发送 MQTT 消息失败", e);
            throw new RuntimeException("发送 MQTT 消息失败", e);
        }
    }
    
    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }
    
    @Override
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }
    
    @Override
    public String getProtocolType() {
        return TerminalProtocolType.MQTT.name();
    }
    
    /**
     * 构建 MQTT Broker URL
     */
    private String buildBrokerUrl(TerminalConfig config) {
        String url = config.getServerUrl();
        if (url == null || url.isEmpty()) {
            url = "mqtt://localhost";
        }
        return url;
    }
    
    /**
     * 创建 MQTT 连接选项
     */
    private MqttConnectOptions createConnectOptions(TerminalConfig config) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(config.getConnectTimeout() / 1000);
        options.setKeepAliveInterval(config.getHeartbeatInterval() / 1000);
        options.setAutomaticReconnect(true);
        
        // 设置认证信息
        if (config.getProductId() != null && !config.getProductId().isEmpty()) {
            options.setUserName(config.getProductId());
        }
        if (config.getSecretKey() != null && !config.getSecretKey().isEmpty()) {
            options.setPassword(config.getSecretKey().toCharArray());
        }
        
        return options;
    }
    
    /**
     * 获取事件主题
     */
    private String getEventTopic(String terminalId) {
        return "terminals/me/events";
    }
    
    /**
     * 获取动作主题
     */
    private String getActionTopic(String terminalId) {
        return "terminals/" + terminalId + "/actions";
    }

    private String getErrorTopic(String terminalId) {
        return "terminals/" + terminalId + "/errors";
    }
}
