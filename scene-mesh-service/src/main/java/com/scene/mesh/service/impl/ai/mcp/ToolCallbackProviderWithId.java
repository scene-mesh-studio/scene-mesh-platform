package com.scene.mesh.service.impl.ai.mcp;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;

public class ToolCallbackProviderWithId extends SyncMcpToolCallbackProvider {

    private final String serverName;

    public ToolCallbackProviderWithId(String serverName, McpSyncClient... mcpClients) {
        super(mcpClients);
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

}
