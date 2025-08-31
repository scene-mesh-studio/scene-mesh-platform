package com.scene.mesh.model.knowledge;

import lombok.Data;

@Data
public class KnowledgeBase {
    private String id;
    private String name;
    private String description;
    private String[] knowledgeItemIds;
    private String providerName;
    private String modelName;
    private Boolean enabled;
}
