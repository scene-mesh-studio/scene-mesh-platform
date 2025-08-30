package com.scene.mesh.service.spec.ai.advisor;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AdvisorBuildRequest {

    private AdvisorBuildRequestType requestType;

    private Map<String,Object> payload;

    public AdvisorBuildRequest(AdvisorBuildRequestType requestType) {
        this.requestType = requestType;
        this.payload = new HashMap<>();
    }

    public Object getPayloadVal(String key){
        return payload.get(key);
    }

    public void setPayloadEntry(String key,Object val){
        payload.put(key,val);
    }

    public enum AdvisorBuildRequestType{
        knowledge
    }
}
