package com.scene.mesh.service.spec.ai.rag;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

public interface IEmbeddingService {

    String vectorize(String contentId, Resource documentResource, Map<String,Object> vectorizeOptions);

    List<Document> findVectors(String contentId, String providerName, String modelName);

    Pair<Boolean,String> deleteVectorize(String contentId, String providerName, String modelName);
}
