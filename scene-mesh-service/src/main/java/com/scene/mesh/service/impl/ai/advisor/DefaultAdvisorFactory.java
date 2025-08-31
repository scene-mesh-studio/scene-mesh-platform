package com.scene.mesh.service.impl.ai.advisor;

import com.scene.mesh.service.spec.ai.advisor.IAdvisorFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import java.util.List;

public class DefaultAdvisorFactory implements IAdvisorFactory {

    @Override
    public Advisor getKnowledgeAdvisor(List<Pair<String, Integer>> knowledgeBases) {

        return null;
    }
}
