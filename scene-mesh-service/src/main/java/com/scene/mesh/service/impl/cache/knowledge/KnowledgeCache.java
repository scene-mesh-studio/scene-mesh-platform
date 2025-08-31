package com.scene.mesh.service.impl.cache.knowledge;

import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.model.knowledge.KnowledgeBase;
import com.scene.mesh.service.spec.cache.IDisposed;

import java.util.List;

public class KnowledgeCache implements IDisposed {

    private final ICache<String, KnowledgeBase> cache;

    public static String KEY_PREFIX = "knowledge:";

    public KnowledgeCache(ICache<String, KnowledgeBase> cache) {
        this.cache = cache;
    }

    @Override
    public void dispose() {
        this.cache.deleteByKeyPrefix(KEY_PREFIX + "*");
    }

    public List<KnowledgeBase> getAllKnowledgeBases() {
        return this.cache.getAll(KEY_PREFIX + "*");
    }

    public KnowledgeBase getKnowledge(String kbId) {
        return this.cache.get(KEY_PREFIX + kbId);
    }
}
