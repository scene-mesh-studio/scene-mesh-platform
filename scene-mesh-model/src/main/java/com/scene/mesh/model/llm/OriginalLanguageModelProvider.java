package com.scene.mesh.model.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OriginalLanguageModelProvider {

    @JsonProperty("id")
    private String id;

    @JsonProperty("modelName")
    private String modelName; // 例如: "languageModelProvider"

    @JsonProperty("values")
    private ProviderValues values;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    /**
     * 对应于提供商的 "values" 对象，包含了提供商的详细配置信息。
     */
    @Data
    public static class ProviderValues {

        @JsonProperty("name")
        private String name;

        @JsonProperty("image")
        private ImageDetails image;

        @JsonProperty("apiKey")
        private String apiKey;

        @JsonProperty("apiHost")
        private String apiHost;

        @JsonProperty("apiMode")
        private String apiMode;

        @JsonProperty("description")
        private String description;

        @JsonProperty("apiCompatibility")
        private boolean apiCompatibility;

        @JsonProperty("models")
        private List<LanguageModel> models;
    }

    /**
     * 对应于 "image" 对象。
     */
    @Data
    public static class ImageDetails {

        @JsonProperty("fileName")
        private String fileName;

        @JsonProperty("filePath")
        private String filePath;

        @JsonProperty("fileSize")
        private long fileSize;

        @JsonProperty("fileType")
        private String fileType;
    }

    /**
     * 对应于 "models" 数组中的一个具体语言模型。
     */
    @Data
    public static class LanguageModel {

        @JsonProperty("id")
        private String id;

        @JsonProperty("modelName")
        private String modelName; // 例如: "languageModel"

        @JsonProperty("values")
        private ModelDetails values;

        @JsonProperty("isDeleted")
        private boolean isDeleted;
    }

    /**
     * 对应于具体模型的 "values" 对象。
     */
    @Data
    public static class ModelDetails {

        @JsonProperty("name")
        private String name;

        @JsonProperty("feature")
        private List<String> feature;

        @JsonProperty("description")
        private String description;

        @JsonProperty("modelPath")
        private String modelPath;

        @JsonProperty("dimensions")
        private int dimensions;
    }
}