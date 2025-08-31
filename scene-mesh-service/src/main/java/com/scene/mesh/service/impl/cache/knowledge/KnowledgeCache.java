package com.scene.mesh.service.impl.cache.knowledge;

import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.model.mcp.McpServer;
import com.scene.mesh.service.spec.cache.IDisposed;

import java.util.List;

public class KnowledgeCache implements IDisposed {

    private final ICache<String, McpServer> cache;

    public static String KEY_PREFIX = "mcp:";

    public KnowledgeCache(ICache<String, McpServer> cache) {
        this.cache = cache;
    }

    @Override
    public void dispose() {
        this.cache.deleteByKeyPrefix(KEY_PREFIX + "*");
    }

    public List<McpServer> getAllMcpServers() {
        return this.cache.getAll(KEY_PREFIX + "*");
    }

}
