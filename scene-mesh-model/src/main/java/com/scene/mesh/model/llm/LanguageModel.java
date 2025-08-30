package com.scene.mesh.model.llm;

import lombok.Data;

import java.util.List;

@Data
public class LanguageModel {
    private String id;
    private String name;
    private String description;
    private List<String> feature; // type: 'array' of enum -> List<String>
    private String modelPath;
    private int dimensions;
}
