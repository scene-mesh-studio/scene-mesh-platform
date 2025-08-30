package com.scene.mesh.service.impl.ai.rag.openai;

import com.scene.mesh.service.spec.ai.rag.IEmbeddingModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;

public class CompatibleOpenAiEmbeddingModel implements IEmbeddingModel {

    private final String modelId;

    private final String modelName;

    private final String provider;

    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    public CompatibleOpenAiEmbeddingModel(String modelId, String modelName, String provider, String baseUrl, String embeddingPath,int dimensions, String apiKey) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.provider = provider;
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).embeddingsPath(embeddingPath).apiKey(apiKey).build();

        OpenAiEmbeddingOptions.Builder builder= OpenAiEmbeddingOptions.builder()
                .model(modelName);

        if (dimensions>0){
            builder.dimensions(dimensions);
        }

        OpenAiEmbeddingOptions embeddingOptions = builder.build();

        this.openAiEmbeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.ALL, embeddingOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getProvider() {
        return provider;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return this.openAiEmbeddingModel.call(request);
    }

    @Override
    public float[] embed(Document document) {
        return this.openAiEmbeddingModel.embed(document);
    }
}
