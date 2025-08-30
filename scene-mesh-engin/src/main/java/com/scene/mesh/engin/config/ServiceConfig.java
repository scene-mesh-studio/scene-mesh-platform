package com.scene.mesh.engin.config;

import com.scene.mesh.foundation.spec.api.ApiClient;
import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.service.impl.ai.mcp.DefaultIToolsService;
import com.scene.mesh.service.impl.ai.config.DefaultLLmConfigService;
import com.scene.mesh.service.impl.ai.mcp.DefaultMcpServerService;
import com.scene.mesh.service.impl.ai.mcp.ToolCallbackProviderManager;
import com.scene.mesh.service.impl.event.DefaultMetaEventService;
import com.scene.mesh.service.impl.product.DefaultProductService;
import com.scene.mesh.service.impl.scene.DefaultSceneService;
import com.scene.mesh.service.spec.ai.config.ILLmConfigService;
import com.scene.mesh.service.spec.ai.mcp.IMcpServerService;
import com.scene.mesh.service.spec.ai.mcp.IToolsService;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import com.scene.mesh.service.spec.event.IMetaEventService;
import com.scene.mesh.service.spec.product.IProductService;
import com.scene.mesh.service.spec.scene.ISceneService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    // AI配置
    @Value("${scene-mesh.ai.provider.zhipu.api-key}")
    private String zhipuApiKey;

    @Bean
    public MutableCacheService mutableCache(ICache iCache, ApiClient apiClient) {
        return new MutableCacheService(iCache,apiClient);
    }

    @Bean
    public IMetaEventService metaEventService(MutableCacheService mutableCacheService) {
        return new DefaultMetaEventService(mutableCacheService);
    }

    @Bean
    public IProductService productService(MutableCacheService mutableCacheService) {
        return new DefaultProductService(mutableCacheService);
    }

    @Bean
    public ISceneService sceneService(MutableCacheService mutableCacheService) {
        return new DefaultSceneService(mutableCacheService);
    }

//    @Bean
//    public ZhiPuChatModel zhiPuChatModel() {
//        return new ZhiPuChatModel(zhipuApiKey);
//    }
//
    @Bean
    public IMcpServerService mcpServerService(MutableCacheService mutableCacheService) {
        return new DefaultMcpServerService(mutableCacheService);
    }

    @Bean
    public ILLmConfigService lLmConfigService(MutableCacheService mutableCacheService) {
        return new DefaultLLmConfigService(mutableCacheService);
    }

    @Bean
    public IToolsService toolsService(ToolCallbackProviderManager toolCallbackProviderManager) {
        return new DefaultIToolsService(toolCallbackProviderManager);
    }
}
