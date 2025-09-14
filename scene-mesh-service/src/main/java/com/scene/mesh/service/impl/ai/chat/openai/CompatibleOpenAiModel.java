package com.scene.mesh.service.impl.ai.chat.openai;

import com.scene.mesh.service.impl.ai.chat.BaseChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
public class CompatibleOpenAiModel extends BaseChatModel {

    private final String modelId;

    private final String modelName;

    private final String provider;

    private final OpenAiChatModel openAiChatModel;

    private int connectionTimeoutMs;

    private int readTimeoutMs;

    public CompatibleOpenAiModel(String modelId, String modelName, String provider, String baseUrl, String path, String apiKey) {
        this(modelId, modelName, provider, baseUrl, path, apiKey, 3000, 30000);
    }

    public CompatibleOpenAiModel(String modelId, String modelName, String provider, String baseUrl, String path, String apiKey, int connectionTimeoutMs, int readTimeoutMs) {
        super();
        this.modelId = modelId;
        this.modelName = modelName;
        this.provider = provider;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectionTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(requestFactory);


        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .completionsPath(path)
                .restClientBuilder(restClientBuilder)
                .apiKey(apiKey)
                .build();
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
