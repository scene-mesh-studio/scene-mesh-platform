package com.scene.mesh.service.impl.ai.chat.zhipu;

import com.scene.mesh.service.impl.ai.chat.BaseChatModel;
import com.scene.mesh.service.impl.ai.chat.ChatModelD;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;

public class ZhiPuChatModel extends BaseChatModel {

    private final ZhiPuAiChatModel chatModel;

    public ZhiPuChatModel(String apiKey) {
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder().build();

        ZhiPuAiApi zhiPuAiApi =
                new ZhiPuAiApi(apiKey);
        chatModel = new ZhiPuAiChatModel(zhiPuAiApi,options);
    }

    @Override
    public String getModelId() {
        return ChatModelD.ZhiPu_GLM_4_Flash.name();
    }

    @Override
    public String getModelName() {
        return "智谱 GLM_4_flash 免费模型";
    }

    @Override
    public String getProvider() {
        return "智谱";
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.chatModel.call(prompt);
    }

//    @Override
//    public ChatOptions getDefaultOptions() {
//        ChatOptions superOptions = this.chatModel.getDefaultOptions();
//        ZhiPuAiChatOptions defaultOptions = ZhiPuAiChatOptions.fromOptions((ZhiPuAiChatOptions) superOptions);
//        defaultOptions.setModel(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue());
//        return defaultOptions;
//    }
}
