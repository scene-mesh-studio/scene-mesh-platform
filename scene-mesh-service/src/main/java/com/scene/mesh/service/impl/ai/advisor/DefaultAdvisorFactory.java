package com.scene.mesh.service.impl.ai.advisor;

import com.scene.mesh.model.knowledge.KnowledgeBase;
import com.scene.mesh.service.impl.ai.rag.MultiKnowledgeQuestionAnswerAdvisor;
import com.scene.mesh.service.spec.ai.advisor.IAdvisorFactory;
import com.scene.mesh.service.spec.ai.knowledge.IKnowledgeService;
import com.scene.mesh.service.spec.ai.rag.IVectorStoreFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefaultAdvisorFactory implements IAdvisorFactory {

    private final List<Advisor> advisors;

    private final IKnowledgeService knowledgeService;

    private final IVectorStoreFactory vectorStoreFactory;

    public DefaultAdvisorFactory(IKnowledgeService knowledgeService, IVectorStoreFactory vectorStoreFactory) {
        this.knowledgeService = knowledgeService;
        this.vectorStoreFactory = vectorStoreFactory;
        this.advisors = initDefaultAdvisors();
    }

    private List<Advisor> initDefaultAdvisors() {
        List<Advisor> advisors = new ArrayList<>();
        //TODO messageMemory
        return advisors;
    }

    @Override
    public List<Advisor> getMutableAdvisors(Map<String, Object> advisorsInput) {
        if (advisorsInput == null || advisorsInput.isEmpty()) {
            return new ArrayList<>();
        }

        if (advisorsInput.containsKey("knowledgeBase")) {
            Pair[] kps = (Pair[]) advisorsInput.get("knowledgeBase");
            Advisor knowledgeAdvisor = getKnowledgeAdvisor(kps);
            if (knowledgeAdvisor != null) {
                advisors.add(knowledgeAdvisor);
            }
        }
        return advisors;
    }

    private Advisor getKnowledgeAdvisor(Pair<String, Integer>[] knowledgeBases) {
        if (knowledgeBases == null || knowledgeBases.length == 0) {
            return null;
        }

        List<Pair<Integer, VectorStore>> vectorStores = new ArrayList<>();
        for (Pair<String, Integer> knowledgeBasePair : knowledgeBases) {
            String kbId = knowledgeBasePair.getLeft();
            Integer priority = knowledgeBasePair.getRight();

            KnowledgeBase knowledgeBase = this.knowledgeService.getKnowledge(kbId);
            if (knowledgeBase == null) {
                continue;
            }
            String providerName = knowledgeBase.getProviderName();
            String modelName = knowledgeBase.getModelName();

            VectorStore vs = this.vectorStoreFactory.getVectorStore(providerName, modelName);
            if (vs == null) {
                continue;
            }
            vectorStores.add(Pair.of(priority, vs));
        }
        return new MultiKnowledgeQuestionAnswerAdvisor(vectorStores);
    }
}
