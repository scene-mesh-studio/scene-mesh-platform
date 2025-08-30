package com.scene.mesh.service.impl.ai.rag;

import lombok.Data;

import java.util.List;

@Data
public class VectorSearchBody {
    private String providerName;
    private String modelName;
    private List<String> contentIds;
}
