package com.scene.mesh.service.impl.ai.chat.openai;

import com.scene.mesh.service.impl.ai.chat.BaseChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class CompatibleOpenAiModel extends BaseChatModel {

    private final String modelId;

    private final String modelName;

    private final String provider;

    private final OpenAiChatModel openAiChatModel;

    public CompatibleOpenAiModel(String modelId, String modelName, String provider, String baseUrl, String path, String apiKey) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.provider = provider;
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).completionsPath(path).apiKey(apiKey).build();
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().model(modelName).build();
        this.openAiChatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatOptions).build();
    }

    @Override
    public String getModelId() {
        return this.modelId;
    }

    @Override
    public String getModelName() {
        return this.modelName;
    }

    @Override
    public String getProvider() {
        return this.provider;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.openAiChatModel.call(prompt);
    }
}
