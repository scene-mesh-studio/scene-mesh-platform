package com.scene.mesh.engin.config;

import com.scene.mesh.foundation.impl.cache.RedisCache;
import com.scene.mesh.foundation.impl.component.SpringComponentProvider;
import com.scene.mesh.foundation.impl.message.JsonMessageSerializer;
import com.scene.mesh.foundation.impl.message.RedisMessageConsumer;
import com.scene.mesh.foundation.impl.message.RedisMessageProducer;
import com.scene.mesh.foundation.spec.api.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FoundationConfig {

    @Value("${scene-mesh.infrastructure.redis.host}")
    private String redisHost;

    @Value("${scene-mesh.infrastructure.redis.port}")
    private String redisPort;

    @Value("${scene-mesh.infrastructure.message.batch-size}")
    private String batchSize;

    @Value("${scene-mesh.infrastructure.message.timeout-seconds}")
    private String timeoutSeconds;

    @Value("${scene-mesh.api-client.urls.product}")
    private String productUrl;

    @Value("${scene-mesh.api-client.urls.llm}")
    private String llmUrl;

    @Value("${scene-mesh.api-client.urls.mcpserver}")
    private String mcpServerUrl;

    @Value("${scene-mesh.api-client.urls.knowledgeBase}")
    private String knowledgebaseUrl;


    @Bean
    public SpringComponentProvider componentProvider() {
        return new SpringComponentProvider();
    }

    @Bean
    public RedisCache iCache() {
        return new RedisCache(redisHost, Integer.parseInt(redisPort));
    }

    @Bean
    public RedisMessageConsumer messageConsumer() {
        RedisMessageConsumer consumer = new RedisMessageConsumer();
        consumer.setBatchSize(Integer.parseInt(batchSize));
        consumer.setHost(redisHost);
        consumer.setPort(Integer.parseInt(redisPort));
        consumer.setTimeoutSeconds(Integer.parseInt(timeoutSeconds));
        consumer.setSerializer(new JsonMessageSerializer());
        consumer.__init__();
        return consumer;
    }

    @Bean
    public RedisMessageProducer messageProducer() {
        RedisMessageProducer producer = new RedisMessageProducer();
        producer.setHost(redisHost);
        producer.setPort(Integer.parseInt(redisPort));
        producer.setSerializer(new JsonMessageSerializer());
        producer.__init__();
        return producer;
    }

    @Bean
    public ApiClient apiClient(){
        Map<String,String> urls = new HashMap<>();
        urls.put(ApiClient.ServiceType.product.name(),productUrl);
        urls.put(ApiClient.ServiceType.llm.name(),llmUrl);
        urls.put(ApiClient.ServiceType.mcpserver.name(),mcpServerUrl);
        urls.put(ApiClient.ServiceType.knowledgebase.name(),knowledgebaseUrl);
        ApiClient apiClient = new ApiClient(urls);
        apiClient.__init__();
        return apiClient;
    }

}
