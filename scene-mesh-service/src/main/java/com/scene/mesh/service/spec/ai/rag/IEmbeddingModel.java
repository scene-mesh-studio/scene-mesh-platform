package com.scene.mesh.service.spec.ai.rag;

import org.springframework.ai.embedding.EmbeddingModel;

import java.io.Serializable;

public interface IEmbeddingModel extends EmbeddingModel, Serializable {
    /**
     * 获取模型ID
     *
     * @return 模型ID
     */
    String getModelId();

    /**
     * 模型名称
     * @return 模型名称
     */
    String getModelName();

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProvider();
}

