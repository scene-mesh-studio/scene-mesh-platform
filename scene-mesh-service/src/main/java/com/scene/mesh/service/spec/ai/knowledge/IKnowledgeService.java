package com.scene.mesh.service.spec.ai.knowledge;

import com.scene.mesh.model.knowledge.KnowledgeBase;

public interface IKnowledgeService {

    KnowledgeBase getKnowledge(String kbId);
}
