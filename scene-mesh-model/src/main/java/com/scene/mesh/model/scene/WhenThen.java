package com.scene.mesh.model.scene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import com.scene.mesh.model.product.OutputActionsDeserializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhenThen {

    private String when;
    private Then then;

    @JsonSetter("when")
    public void setWhen(Object when) {
        this.when = SimpleObjectHelper.objectData2json(when);
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Then {
        private String id;
        private String name;
        private String label;
        private String type;
        private String modelProvider;
        private String model;
        private String promptTemplate;
        private String[] promptVariables;
        private Double temperature;
        private Integer topP;
        private String[] mcps;
        private KnowledgeBase[] knowledgeBases;
        @JsonDeserialize(using = OutputActionsDeserializer.class)
        private List<OutputAction> outputActions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeBase {
        private String id;
        private int priority;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutputAction {
        private String actionId;
        private List<OutPutActionValue> values = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutPutActionValue {
        private String fieldName;
        private String value;
    }
}
