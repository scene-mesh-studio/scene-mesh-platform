package com.scene.mesh.service.impl.ai.knowledge;

import com.scene.mesh.model.knowledge.KnowledgeBase;
import com.scene.mesh.service.spec.ai.knowledge.IKnowledgeService;
import com.scene.mesh.service.spec.cache.MutableCacheService;

public class DefaultKnowledgeService implements IKnowledgeService {

    private final MutableCacheService mutableCacheService;

    public DefaultKnowledgeService(MutableCacheService mutableCacheService) {
        this.mutableCacheService = mutableCacheService;
    }

    @Override
    public KnowledgeBase getKnowledge(String kbId) {
        return mutableCacheService.getKnowledgeById(kbId);
    }
}
