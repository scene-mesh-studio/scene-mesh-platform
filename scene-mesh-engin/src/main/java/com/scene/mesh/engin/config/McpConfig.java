package com.scene.mesh.engin.config;

import com.scene.mesh.model.mcp.McpServer;
import com.scene.mesh.service.impl.ai.mcp.ToolCallbackProviderManager;
import com.scene.mesh.service.impl.ai.mcp.ToolCallbackProviderWithId;
import com.scene.mesh.service.spec.ai.mcp.IMcpServerService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@Slf4j
public class McpConfig {

    @Value("${scene-mesh.ai.mcp.server.url}")
    private String mcpServerUrl;

    private ToolCallbackProviderWithId actionToolCallbackProvider() {
        // 创建 SSE 传输层
        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(mcpServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);

        McpClientTransport transport = WebFluxSseClientTransport
                .builder(webClientBuilder)
                .sseEndpoint("/sse")
                .build();
        try {
            McpSyncClient mcpClient = McpClient.sync(transport).build();
            mcpClient.initialize();
            return new ToolCallbackProviderWithId("action", mcpClient);
        } catch (Exception e) {
            log.error("Failed to initialize Scene Mesh MCP client", e);
            return null;
        }
    }

    private List<ToolCallbackProvider> realToolCallbackProviders(IMcpServerService mcpServerService) {

        List<McpServer> mcpServers = mcpServerService.getAllMcpServers();
        if (mcpServers == null || mcpServers.isEmpty()) return new ArrayList<>();

        List<ToolCallbackProvider> toolCallbackProviders = new ArrayList<>();
        for (McpServer mcpServer : mcpServers) {

            McpClientTransport transport = null;

            if ("sse".equals(mcpServer.getType())) {
                WebClient.Builder webClientBuilder = WebClient.builder()
                        .baseUrl(mcpServer.getBaseUrl())
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);
                transport = WebFluxSseClientTransport
                        .builder(webClientBuilder)
                        .sseEndpoint(mcpServer.getEndpoint())
                        .build();
            } else if ("streamable".equals(mcpServer.getType())) {
                WebClient.Builder webClientBuilder = WebClient.builder()
                        .baseUrl(mcpServer.getBaseUrl())
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);
                transport = WebClientStreamableHttpTransport
                        .builder(webClientBuilder)
                        .endpoint(mcpServer.getEndpoint())
                        .build();
            } else {
                throw new RuntimeException("Unsupported MCP server type: " + mcpServer.getType());
            }

            McpSyncClient mcpSyncClient = McpClient
                    .sync(transport)
                    .requestTimeout(Duration.ofSeconds(mcpServer.getTimeout()))
                    .build();
            mcpSyncClient.initialize();

            ToolCallbackProvider toolCallbackProvider = new ToolCallbackProviderWithId(mcpServer.getId(), mcpSyncClient);
            toolCallbackProviders.add(toolCallbackProvider);
        }

        return toolCallbackProviders;
    }

    @Bean
    public ToolCallbackProviderManager sceneMeshToolCallbackProvider(IMcpServerService mcpServerService) {

        ToolCallbackProviderManager toolCallbackProviderManager = new ToolCallbackProviderManager();

        // action mcp tools
        ToolCallbackProviderWithId actionToolCallbackProvider = actionToolCallbackProvider();
        if (actionToolCallbackProvider != null) {
            toolCallbackProviderManager.registerToolCallbackProvider(actionToolCallbackProvider);
        }

        // real mcp tools
        List<ToolCallbackProvider> realToolCallbackProviders = realToolCallbackProviders(mcpServerService);

        for (ToolCallbackProvider toolCallbackProvider : realToolCallbackProviders) {
            toolCallbackProviderManager.registerToolCallbackProvider((ToolCallbackProviderWithId) toolCallbackProvider);
        }

        // 打印工具详情
        Collection<ToolCallbackProvider> toolCallbackProviders = toolCallbackProviderManager.getAllToolCallbackProvider();
        for (ToolCallbackProvider provider : toolCallbackProviders) {
            ToolCallback[] callbacks = provider.getToolCallbacks();
            log.info("🔍 从MCP服务器获取到 {} 个工具:", callbacks.length);
            for (int i = 0; i < callbacks.length; i++) {
                log.info("  {}. {} - {}", i + 1,
                        callbacks[i].getToolDefinition().name(),
                        callbacks[i].getToolDefinition().description());
            }
        }
        return toolCallbackProviderManager;
    }
}
