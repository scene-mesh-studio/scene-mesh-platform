package com.scene.mesh.service.spec.ai.chat;

import org.springframework.ai.chat.client.ChatClient;

/**
 * chat client 工厂
 */
public interface IChatClientFactory {

    ChatClient getChatClient(String providerName, String modelName);

//    ChatOptions getDefaultChatOptions(String providerName, String modelName);
}
