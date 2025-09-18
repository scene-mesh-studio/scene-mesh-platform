package com.scene.mesh.facade.impl.config;

import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.foundation.spec.message.IMessageConsumer;
import com.scene.mesh.foundation.spec.message.IMessageProducer;
import com.scene.mesh.foundation.impl.cache.RedisCache;
import com.scene.mesh.foundation.impl.message.JsonMessageSerializer;
import com.scene.mesh.foundation.impl.message.RedisMessageConsumer;
import com.scene.mesh.foundation.impl.message.RedisMessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoundationConfig {

    @Value("${redis.connection.host}")
    private String redisHost;

    @Value("${redis.connection.port}")
    private int redisPort;

    @Bean
    public ICache<String,Object> redisCache(){
        return new RedisCache<>(redisHost,redisPort);
    }

    @Bean
    public IMessageProducer messageProducer(){
        RedisMessageProducer messageProducer = new RedisMessageProducer();
        messageProducer.setHost(redisHost);
        messageProducer.setPort(redisPort);
        messageProducer.setSerializer(new JsonMessageSerializer());
        messageProducer.__init__();
        return messageProducer;
    }

    @Bean
    public IMessageConsumer messageConsumer(){
        RedisMessageConsumer messageConsumer = new RedisMessageConsumer();
        messageConsumer.setHost(redisHost);
        messageConsumer.setPort(redisPort);
        messageConsumer.setSerializer(new JsonMessageSerializer());
        messageConsumer.__init__();
        return messageConsumer;
    }
}
