package com.scene.mesh.foundation.impl.processor.flink;

import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import com.scene.mesh.foundation.spec.component.IComponentProvider;
import com.scene.mesh.foundation.spec.processor.config.ProcessorGraph;
import com.scene.mesh.foundation.spec.processor.execute.IProcessExecutor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.*;

import java.time.Duration;

/**
 * flink 处理器
 */
@Slf4j
public class FlinkProcessExecutor implements IProcessExecutor {

    private final IComponentProvider componentProvider;
    private Configuration configuration;
    @Setter
    private String webHost;
    @Setter
    private int webPort;

    public void __init__() {
        this.configuration = new Configuration();
        // Rest 配置
//        configuration.set(RestOptions.PORT,webPort);
//        configuration.set(RestOptions.ADDRESS, webHost);
        // 设置重启策略为固定延迟重启
        configuration.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 3);
        configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofSeconds(10));
        // 合理设置内存配置（以MB为单位而非GB）
        configuration.set(TaskManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.ofMebiBytes(1024)); // 1GB
        configuration.set(TaskManagerOptions.MANAGED_MEMORY_SIZE, MemorySize.ofMebiBytes(256)); // 256MB
        configuration.set(TaskManagerOptions.TASK_HEAP_MEMORY, MemorySize.ofMebiBytes(512)); // 512MB
        configuration.set(TaskManagerOptions.TASK_OFF_HEAP_MEMORY, MemorySize.ofMebiBytes(128)); // 128MB
    }

    public FlinkProcessExecutor(IComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public void execute(ProcessorGraph processorGraph, String... args) throws Exception {
        String env = System.getProperty("execute.env");
        if (env != null) {
            this.configuration.setString("execute.env", env);
            String json = SimpleObjectHelper.strMap2json(configuration.toMap());
            log.info("FlinkProcessExecutor Configuration json:{}", json);
            log.info("传递环境变量到 Flink Configuration: execute.env={}", env);
        }
        FlinkProcessActuator actuator =
                new FlinkProcessActuator(this.componentProvider, this.configuration);
        actuator.initialize(processorGraph, args);
        actuator.launch();
    }
}
