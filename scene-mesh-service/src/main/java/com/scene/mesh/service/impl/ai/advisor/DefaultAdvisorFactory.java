package com.scene.mesh.service.impl.ai.advisor;

import com.scene.mesh.service.spec.ai.advisor.AdvisorBuildRequest;
import com.scene.mesh.service.spec.ai.advisor.IAdvisorFactory;
import org.springframework.ai.chat.client.advisor.api.Advisor;

public class DefaultAdvisorFactory implements IAdvisorFactory {
    @Override
    public Advisor getAdvisors(AdvisorBuildRequest request) {
        if (AdvisorBuildRequest.AdvisorBuildRequestType.knowledge.equals(request.getRequestType())){
            return fetchKnowledgeAdvisor(request);
        }
        return null;
    }

    private Advisor fetchKnowledgeAdvisor(AdvisorBuildRequest request) {
//        List<VectorSearchBody> modelList = (List<VectorSearchBody>) request.getPayloadVal("model-list");
//        List<VectorSearchBody> modelList
        return null;
    }
}
