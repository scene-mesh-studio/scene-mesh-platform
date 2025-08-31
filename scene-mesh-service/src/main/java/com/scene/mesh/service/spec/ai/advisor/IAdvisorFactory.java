package com.scene.mesh.service.spec.ai.advisor;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import java.util.List;

public interface IAdvisorFactory {

    Advisor getKnowledgeAdvisor(List<Pair<String, Integer>> knowledgeBases);

}
