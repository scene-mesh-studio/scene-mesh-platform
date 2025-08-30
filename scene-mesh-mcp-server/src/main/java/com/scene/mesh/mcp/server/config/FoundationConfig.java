package com.scene.mesh.mcp.server.config;

import com.scene.mesh.foundation.spec.message.IMessageProducer;
import com.scene.mesh.foundation.impl.message.JsonMessageSerializer;
import com.scene.mesh.foundation.impl.message.RedisMessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoundationConfig {

    @Value("${sm.redis.host}")
    private String redisHost;

    @Value("${sm.redis.port}")
    private int redisPort;

    @Bean
    public IMessageProducer messageProducer(){
        RedisMessageProducer messageProducer = new RedisMessageProducer();
        messageProducer.setHost(redisHost);
        messageProducer.setPort(redisPort);
        messageProducer.setSerializer(new JsonMessageSerializer());
        messageProducer.__init__();
        return messageProducer;
    }
}
