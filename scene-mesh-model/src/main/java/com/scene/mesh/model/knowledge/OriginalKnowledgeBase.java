package com.scene.mesh.model.knowledge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.scene.mesh.model.llm.OriginalLanguageModelProvider;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OriginalKnowledgeBase {

    @JsonProperty("id")
    private String id;

    @JsonProperty("modelName")
    private String modelName; // 例如: "mcpService"

    @JsonProperty("values")
    private KnowledgeBaseDetails values;

    @JsonProperty("isDeleted")
    private boolean isDeleted;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeBaseDetails {

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("provider")
        private OriginalLanguageModelProvider provider;

        @JsonProperty("embeddingsModel")
        private OriginalLanguageModelProvider.LanguageModel embeddingsModel;

        @JsonProperty("knowledgeItems")
        private KnowledgeItem[] knowledgeItems;

        @JsonProperty("enabled")
        private Boolean enabled;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeItem {

        @JsonProperty("id")
        private String id;

    }
}
