package com.scene.mesh.service.spec.ai.rag;

import org.springframework.ai.vectorstore.VectorStore;

public interface IVectorStoreFactory {

    VectorStore getVectorStore(String modelProviderName, String embeddingModelName);

}
