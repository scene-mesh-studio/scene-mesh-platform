package com.scene.mesh.service.impl.cache.knowledge;

import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.model.knowledge.KnowledgeBase;
import com.scene.mesh.service.spec.cache.ICacheProvider;

import java.util.List;

public class KnowledgeCacheProvider implements ICacheProvider<KnowledgeCache, KnowledgeBase> {

    private final ICache<String, KnowledgeBase> cache;

    public KnowledgeCacheProvider(ICache cache) {
        this.cache = cache;
    }

    @Override
    public KnowledgeCache generateCacheObject() {
        return new KnowledgeCache(cache);
    }

    @Override
    public KnowledgeCache refreshCacheObject(List<KnowledgeBase> knowledgeBases) {
        if (knowledgeBases.isEmpty()) {return new KnowledgeCache(cache);}

        for (KnowledgeBase knowledgeBase: knowledgeBases) {
            this.cache.set(KnowledgeCache.KEY_PREFIX + knowledgeBase.getId(), knowledgeBase);
        }
        return new KnowledgeCache(cache);
    }
}
