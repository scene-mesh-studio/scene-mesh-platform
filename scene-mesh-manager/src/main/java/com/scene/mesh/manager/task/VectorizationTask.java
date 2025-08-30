package com.scene.mesh.manager.task;

import java.util.Map;
import java.util.UUID;

public class VectorizationTask extends BaseGeneralTask{

    public VectorizationTask() {
        super(UUID.randomUUID().toString(), TaskType.vectorization);
    }

    public String getContentUrl(){
        return (String) this.getPayloadVal("content_url");
    }

    public String getContentId(){
        return  (String) this.getPayloadVal("content_id");
    }

    public Map<String, Object> getOptions() {
        Map<String,Object> options = (Map<String, Object>) this.getPayloadVal("options");
        return options;
    }

}
