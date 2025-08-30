package com.scene.mesh.model.llm;

import lombok.Data;

import java.util.List;

@Data
public class LanguageModelProvider {
    private String id;
    private String name;
    private String description;
    private String image; // type: 'binary' -> String (URL or path)
    private String apiMode; // type: 'enum' -> String
    private String apiHost;
    private String apiKey;
    private Boolean apiCompatibility;
    private List<LanguageModel> models; // type: 'one_to_many'

}
