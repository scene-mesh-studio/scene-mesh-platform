package com.scene.mesh.foundation.impl.message;

import com.scene.mesh.foundation.spec.message.IMessageConsumer;
import com.scene.mesh.foundation.spec.message.IMessageSerializer;
import com.scene.mesh.foundation.spec.message.MessageTopic;
import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.SystemUtils.getHostName;

@Slf4j
public class RedisMessageConsumer implements IMessageConsumer {

    @Setter
    @Getter
    private String host;

    @Setter
    @Getter
    private int port;

    private IMessageSerializer serializer;
    private RedisConnectionFactory connectionFactory;
    private RedisTemplate<String, Object> redisTemplate;
    private StreamOperations<String, Object, Object> streamOperations;

    @Setter
    private int batchSize = 10;

    @Setter
    private int timeoutSeconds = 1;

    private String consumerGroup;
    private String consumerName;

    private boolean isShutdown = false;

    public void __init__() {

        // 配置连接池
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(20);          // 最大连接数
        poolConfig.setMaxIdle(10);           // 最大空闲连接
        poolConfig.setMinIdle(5);            // 最小空闲连接
        poolConfig.setMaxWaitMillis(2000);   // 获取连接最大等待时间

        // 创建Lettuce客户端配置
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(Duration.ofSeconds(3))  // 命令超时
                .shutdownTimeout(Duration.ofSeconds(2)) // 关闭超时
                .build();

        // create redis factory
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        this.connectionFactory = new LettuceConnectionFactory(config,clientConfig);
        ((LettuceConnectionFactory) this.connectionFactory).afterPropertiesSet();

        // create redis template
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        redisTemplate.setDefaultSerializer(jsonSerializer);

        // init RedisTemplate
        redisTemplate.afterPropertiesSet();

        // get StreamOperations
        this.streamOperations = this.redisTemplate.opsForStream();

        this.consumerGroup = "sm-group";
        this.consumerName = "consumer-" + getHostName();
    }

    public void shutdown() {
        if (connectionFactory instanceof LettuceConnectionFactory) {
            ((LettuceConnectionFactory) connectionFactory).destroy();
            isShutdown = true;
        }
    }

    @Override
    public void setSerializer(IMessageSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public <T> List<T> receive(MessageTopic topic, Class<T> messageType) throws Exception {

        if (isShutdown) {
            throw new Exception("message consumer is shutdown");
        }

        String streamKey = topic.getTopicName();
        List<T> messages = new ArrayList<>();

        try {
            try {
                // 创建消费者组
                redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
            }catch (Exception e){
                // 消费者组存在
            }
            // 从Stream读取消息
            List<MapRecord<String, Object, Object>> streamRecords = streamOperations.read(
                    Consumer.from(consumerGroup, consumerName),
                    StreamReadOptions.empty()
                            .count(batchSize)
                            .block(Duration.ofSeconds(timeoutSeconds)),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );

            if (streamRecords != null && !streamRecords.isEmpty()) {
                for (MapRecord<String, Object, Object> record : streamRecords) {
                    // 获取消息内容
                    Map<Object, Object> body = record.getValue();

                    // 获取"data"字段的值
                    if (body.containsKey("data")) {
                        String data = (String) body.get("data");

                        // 反序列化消息
                        T msg = this.serializer.deserialize(data.getBytes(StandardCharsets.UTF_8), messageType);
                        messages.add(msg);
                    }

                    // 确认消息已处理(ACK)
                    streamOperations.acknowledge(consumerGroup, record);
                }
            }

            return messages.isEmpty() ? null : messages;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}