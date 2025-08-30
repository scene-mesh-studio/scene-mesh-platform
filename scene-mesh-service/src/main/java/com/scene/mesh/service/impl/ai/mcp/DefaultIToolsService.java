package com.scene.mesh.service.impl.ai.mcp;

import com.scene.mesh.service.spec.ai.mcp.IToolsService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.ArrayList;
import java.util.List;

public class DefaultIToolsService implements IToolsService {

    private final ToolCallbackProviderManager toolCallbackProviderManager;

    public DefaultIToolsService(ToolCallbackProviderManager toolCallbackProviderManager) {
        this.toolCallbackProviderManager = toolCallbackProviderManager;
    }

    @Override
    public List<ToolCallback> findToolCallbacks(List<String> actionIds, List<String> mcps) {

        List<ToolCallback> discoveredToolCallbacks = new ArrayList<>();

        if (actionIds != null) {
            ToolCallbackProvider actionTcp = this.toolCallbackProviderManager.getToolCallbackProvider("action");
            ToolCallback[] toolCallbacks = actionTcp.getToolCallbacks();
            for (ToolCallback toolCallback : toolCallbacks) {
                for (String actionId : actionIds) {
                    if (toolCallback.getToolDefinition().name().contains(actionId))
                        discoveredToolCallbacks.add(toolCallback);
                }
            }
        }

        if (mcps != null) {
            for (String mcp : mcps) {
                ToolCallbackProvider mcpTcp = this.toolCallbackProviderManager.getToolCallbackProvider(mcp);
                if (mcpTcp != null) {
                    ToolCallback[] toolCallbacks = mcpTcp.getToolCallbacks();
                    discoveredToolCallbacks.addAll(List.of(toolCallbacks));
                }
            }
        }

        return discoveredToolCallbacks;
    }
}
