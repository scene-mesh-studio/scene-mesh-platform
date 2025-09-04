package com.scene.mesh.manager.config;

import com.scene.mesh.foundation.impl.cache.RedisCache;
import com.scene.mesh.foundation.spec.api.ApiClient;
import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.model.terminal.TerminalRepository;
import com.scene.mesh.service.impl.ai.rag.DefaultEmbeddingService;
import com.scene.mesh.service.impl.ai.config.DefaultLLmConfigService;
import com.scene.mesh.service.impl.ai.rag.DefaultVectorStoreFactory;
import com.scene.mesh.service.spec.ai.rag.IVectorStoreFactory;
import com.scene.mesh.service.impl.terminal.DefaultTerminalService;
import com.scene.mesh.service.spec.ai.rag.IEmbeddingService;
import com.scene.mesh.service.spec.ai.config.ILLmConfigService;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import com.scene.mesh.service.spec.terminal.ITerminalService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ServiceConfig {

    @Value("${scene-mesh.infrastructure.redis.host}")
    private String redisHost;

    @Value("${scene-mesh.infrastructure.redis.port}")
    private int redisPort;

    @Value("${scene-mesh.ai.vector.store.url}")
    private String vectorStoreUrl;

    @Value("${scene-mesh.ai.vector.store.username}")
    private String vectorStoreUsername;

    @Value("${scene-mesh.ai.vector.store.password}")
    private String vectorStorePassword;

    @Value("${scene-mesh.ai.vector.store.driver}")
    private String vectorStoreDriver;

    @Bean
    public ITerminalService terminalService(TerminalRepository terminalRepository){
        return new DefaultTerminalService(terminalRepository);
    }

    @Bean
    public ICache cache(){
        return new RedisCache(redisHost,redisPort);
    }

    @Bean
    public ApiClient apiClient(){
        ApiClient apiClient = new ApiClient(null);
        apiClient.__init__();
        return apiClient;
    }

    @Bean
    public MutableCacheService mutableCacheService(ICache cache, ApiClient apiClient){
        return new MutableCacheService(cache,apiClient);
    }

    @Bean
    public ILLmConfigService ilLmConfigService(MutableCacheService mutableCacheService){
        return new DefaultLLmConfigService(mutableCacheService);
    }

    @Bean
    public IEmbeddingService embeddingService(IVectorStoreFactory vectorStoreFactory){
        return new DefaultEmbeddingService(vectorStoreFactory);
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
}
