package com.scene.mesh.model.knowledge;

import lombok.Data;

@Data
public class KnowledgeBase {
    private String id;
    private String name;
    private String description;
    private String type;
    private String baseUrl;
    private String endpoint;
    private String header;
    private Integer timeout;
    private Boolean enabled;

}
