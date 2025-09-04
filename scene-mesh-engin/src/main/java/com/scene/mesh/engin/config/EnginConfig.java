package com.scene.mesh.engin.config;

import com.scene.mesh.engin.model.SceneMatchedResult;
import com.scene.mesh.engin.processor.cache.CacheProcessor;
import com.scene.mesh.engin.processor.cache.CacheTrigger;
import com.scene.mesh.engin.processor.then.operator.AgentThenOperator;
import com.scene.mesh.engin.processor.then.operator.NonAgentThenOperator;
import com.scene.mesh.engin.processor.then.operator.ThenOperatorManager;
import com.scene.mesh.foundation.spec.message.IMessageConsumer;
import com.scene.mesh.foundation.spec.message.IMessageProducer;
import com.scene.mesh.model.event.Event;
import com.scene.mesh.service.spec.ai.advisor.IAdvisorFactory;
import com.scene.mesh.service.spec.ai.config.ILLmConfigService;
import com.scene.mesh.service.spec.ai.mcp.IToolsService;
import com.scene.mesh.service.spec.event.IMetaEventService;
import com.scene.mesh.service.spec.scene.ISceneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Configuration;
import com.scene.mesh.foundation.impl.component.SpringComponentProvider;
import com.scene.mesh.foundation.impl.processor.execute.DefaultProcessManager;
import com.scene.mesh.foundation.impl.processor.flink.FlinkProcessExecutor;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import com.scene.mesh.engin.processor.when.EventProducer;
import com.scene.mesh.engin.processor.when.EventSinker;
import com.scene.mesh.engin.processor.then.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Configuration
@Slf4j
public class EnginConfig {

    // 添加这个Bean来处理占位符
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        String env = System.getProperty("execute.env");
        if (Objects.isNull(env)) {
            throw new RuntimeException("execute.env is null. Please set execute.env property.");
        }
        String ymlPath = "application-"+env+".yml";
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(ymlPath));
        configurer.setProperties(Objects.requireNonNull(yaml.getObject()));
        return configurer;
    }

    // 基础设施配置
    @Value("${scene-mesh.infrastructure.flink-web.host}")
    private String webHost;

    @Value("${scene-mesh.infrastructure.flink-web.port}")
    private String webPort;

    @Value("${scene-mesh.topics.inbound-events}")
    private String inboundEventsTopic;

    @Value("${scene-mesh.topics.matched-result}")
    private String matchedResultTopic;

    @Value("${scene-mesh.topics.outbound-actions}")
    private String outboundActionsTopic;

    // 消息类配置
    @Value("${scene-mesh.message-classes.event}")
    private String eventMessageClass;

    @Value("${scene-mesh.message-classes.scene-matched-result}")
    private String sceneMatchedResultClass;

    @Bean
    public FlinkProcessExecutor executor(SpringComponentProvider springComponentProvider) {
        FlinkProcessExecutor executor = new FlinkProcessExecutor(springComponentProvider);
        executor.setWebHost(webHost);
        executor.setWebPort(Integer.parseInt(webPort));
        executor.__init__();
        return executor;
    }

    @Bean
    public DefaultProcessManager processManager(FlinkProcessExecutor executor) {
        DefaultProcessManager manager = new DefaultProcessManager();
        manager.setExecutor(executor);
        return manager;
    }

    @Bean
    public AgentThenOperator agentThenOperator(ILLmConfigService llmConfigService,
                                               IToolsService toolsService,
                                               IMetaEventService metaEventService, IAdvisorFactory advisorFactory) {
        return new AgentThenOperator(llmConfigService, toolsService, metaEventService, advisorFactory);
    }

    @Bean
    public NonAgentThenOperator nonAgentThenOperator() {
        return new NonAgentThenOperator();
    }

    @Bean
    public ThenOperatorManager thenOperatorManager(AgentThenOperator agentThenOperator,
                                                   NonAgentThenOperator nonAgentThenOperator) {
        return new ThenOperatorManager(List.of(agentThenOperator, nonAgentThenOperator));
    }

    // ===== When Graph component =====

    @Bean(name = "scene-event-producer")
    public EventProducer sceneEventProducer(IMessageConsumer messageConsumer) throws ClassNotFoundException {
        EventProducer producer = new EventProducer();
        producer.setMessageClass((Class<Event>) Class.forName(eventMessageClass));
        producer.setMessageConsumer(messageConsumer);
        producer.setTopicName(inboundEventsTopic);
        return producer;
    }

    @Bean(name = "scene-match-sinker")
    public EventSinker sceneMatchSinker(IMessageProducer messageProducer) {
        EventSinker sinker = new EventSinker();
        sinker.setTopicName(matchedResultTopic);
        sinker.setMessageProducer(messageProducer);
        return sinker;
    }

    // ===== Then Graph component =====

    @Bean(name = "matched-scene-producer")
    public MatchedSceneProducer matchedSceneProducer(IMessageConsumer messageConsumer) throws ClassNotFoundException {
        MatchedSceneProducer producer = new MatchedSceneProducer();
        producer.setMessageClass((Class<SceneMatchedResult>) Class.forName(sceneMatchedResultClass));
        producer.setMessageConsumer(messageConsumer);
        producer.setTopicName(matchedResultTopic);
        return producer;
    }

    @Bean(name = "scene-selector")
    public SceneSelector sceneSelector(MutableCacheService mutableCacheService, ISceneService sceneService) throws ClassNotFoundException {
        SceneSelector selector = new SceneSelector();
        selector.setCacheService(mutableCacheService);
        selector.setSceneService(sceneService);
        return selector;
    }

    @Bean(name = "then-handler")
    public ThenHandler thenHandler(ISceneService sceneService,ThenOperatorManager operatorManager) {
        return new ThenHandler(sceneService, operatorManager);
    }

    @Bean(name = "action-sinker")
    public ActionSinker actionSinker(IMessageProducer messageProducer) {
        ActionSinker sinker = new ActionSinker();
        sinker.setTopicName(outboundActionsTopic);
        sinker.setMessageProducer(messageProducer);
        return sinker;
    }

    // ======== cache scheduler component =====
    @Bean(name = "cron-trigger")
    public CacheTrigger cacheTrigger(){
        return new CacheTrigger(Duration.ofSeconds(60));
    }

    @Bean(name = "cache-processor")
    public CacheProcessor cacheProcessor(MutableCacheService mutableCacheService){
        return new CacheProcessor(mutableCacheService);
    }
}
