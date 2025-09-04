package com.scene.mesh.engin.config;

import com.scene.mesh.foundation.spec.api.ApiClient;
import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.service.impl.ai.advisor.DefaultAdvisorFactory;
import com.scene.mesh.service.impl.ai.knowledge.DefaultKnowledgeService;
import com.scene.mesh.service.impl.ai.mcp.DefaultIToolsService;
import com.scene.mesh.service.impl.ai.config.DefaultLLmConfigService;
import com.scene.mesh.service.impl.ai.mcp.DefaultMcpServerService;
import com.scene.mesh.service.impl.ai.mcp.ToolCallbackProviderManager;
import com.scene.mesh.service.impl.ai.rag.DefaultVectorStoreFactory;
import com.scene.mesh.service.impl.event.DefaultMetaEventService;
import com.scene.mesh.service.impl.product.DefaultProductService;
import com.scene.mesh.service.impl.scene.DefaultSceneService;
import com.scene.mesh.service.spec.ai.advisor.IAdvisorFactory;
import com.scene.mesh.service.spec.ai.config.ILLmConfigService;
import com.scene.mesh.service.spec.ai.knowledge.IKnowledgeService;
import com.scene.mesh.service.spec.ai.mcp.IMcpServerService;
import com.scene.mesh.service.spec.ai.mcp.IToolsService;
import com.scene.mesh.service.spec.ai.rag.IVectorStoreFactory;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import com.scene.mesh.service.spec.event.IMetaEventService;
import com.scene.mesh.service.spec.product.IProductService;
import com.scene.mesh.service.spec.scene.ISceneService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ServiceConfig {

    @Value("${scene-mesh.ai.vector.store.url}")
    private String vectorStoreUrl;

    @Value("${scene-mesh.ai.vector.store.username}")
    private String vectorStoreUsername;

    @Value("${scene-mesh.ai.vector.store.password}")
    private String vectorStorePassword;

    @Value("${scene-mesh.ai.vector.store.driver}")
    private String vectorStoreDriver;

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

    @Bean
    public IKnowledgeService knowledgeService(MutableCacheService mutableCacheService) {
        return new DefaultKnowledgeService(mutableCacheService);
    }

    @Bean
    public IVectorStoreFactory vectorStoreFactory(ILLmConfigService lLmConfigService){
        
        HikariConfig config = new HikariConfig();

        // 数据库连接配置
        config.setJdbcUrl(vectorStoreUrl);
        config.setUsername(vectorStoreUsername);
        config.setPassword(vectorStorePassword);
        config.setDriverClassName(vectorStoreDriver);

        // 连接池配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // 连接测试配置
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        DataSource dataSource = new HikariDataSource(config);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new DefaultVectorStoreFactory(jdbcTemplate,lLmConfigService);
    }

    @Bean
    public IAdvisorFactory advisorFactory(IKnowledgeService knowledgeService, IVectorStoreFactory vectorStoreFactory) {
        return new DefaultAdvisorFactory(knowledgeService,vectorStoreFactory);
    }
}
