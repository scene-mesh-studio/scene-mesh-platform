package com.scene.mesh.engin;

import com.scene.mesh.engin.config.EnginConfig;
import com.scene.mesh.engin.config.FoundationConfig;
import com.scene.mesh.engin.config.McpConfig;
import com.scene.mesh.engin.config.ServiceConfig;
import com.scene.mesh.engin.model.SceneMatchedResult;
import com.scene.mesh.engin.model.ThenRequest;
import com.scene.mesh.foundation.spec.processor.config.CepModeDescriptor;
import com.scene.mesh.foundation.spec.processor.config.ProcessorGraph;
import com.scene.mesh.foundation.spec.processor.config.ProcessorGraphBuilder;
import com.scene.mesh.foundation.spec.processor.config.ProcessorNodeBuilder;
import com.scene.mesh.foundation.spec.processor.execute.IProcessManager;
import com.scene.mesh.foundation.impl.component.SpringApplicationContextUtils;
import com.scene.mesh.model.action.Action;
import com.scene.mesh.model.event.Event;
import com.scene.mesh.service.impl.scene.DefaultSceneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class SceneMeshEnginApplication {

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new RuntimeException("arguments error. Must enter the 'env' and 'graphId'.");
        }
        String env = args[0];
        String graphId = args[1];
        log.info("execute params - env:{}, graphId:{}", env, graphId);

        List<String> envs = Arrays.asList("dev", "test", "staging", "prod");
        if (!envs.contains(env)) throw new RuntimeException("env is illegal.");
        List<String> graphs = Arrays.asList("when", "then", "scheduler");
        if (!graphs.contains(graphId)) throw new RuntimeException("graphId is illegal.");

        System.setProperty("execute.env", env);

//        Class[] configClasses = new Class[]{
//                EnginConfig.class,
//                FoundationConfig.class,
//                McpConfig.class,
//                ServiceConfig.class
//        };
//
//        SpringApplicationContextUtils.setContextClass(configClasses);
//        log.info("执行引擎启动...  加载引擎配置:{}", Arrays.toString(configClasses));
        SpringApplicationContextUtils.setContextId("work.xml");

        IProcessManager processManager = SpringApplicationContextUtils
                .getApplicationContext()
                .getBean(IProcessManager.class);

        //注册when graph
        ProcessorGraph whenGraph = whenGraph();
        processManager.registerProcess(whenGraph);
        log.info("Graph: {} 已注册， graph信息: {}", whenGraph.getGraphId(), whenGraph);

        //注册 then graph
        ProcessorGraph thenGraph = thenGraph();
        processManager.registerProcess(thenGraph);
        log.info("Graph: {} 已注册， graph信息: {}", thenGraph.getGraphId(), thenGraph);

        //注册 cacheScheduler graph
        ProcessorGraph cacheScheduler = cacheScheduler();
        processManager.registerProcess(cacheScheduler);
        log.info("Graph: {} 已注册， graph信息: {}", cacheScheduler.getGraphId(), cacheScheduler);

        //startup
        try {
            processManager.executeProcesses(graphId,args);
            System.out.println("merger test11");
//            processManager.executeAllProcesses();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private inBoundEventCom(){

    }

    private static ProcessorGraph whenGraph() {

        return ProcessorGraphBuilder.createWithId("when")
                .addNode(ProcessorNodeBuilder.createWithId("scene-event-source")
                        .withComponentId("scene-event-producer")
                        .withParallelism(1)
                        .withOutputType(Event.class))
                .enableCepMode(CepModeDescriptor.builder()
                        .enabled(true)
                        .discoverComponent("sceneService")
                        .period(Duration.ofSeconds(60))
                        .keyed(new String[]{"terminalId"})
                        .parallelism(1)
                        .cepMatchedResultType(new GenericTypeInfo<>(SceneMatchedResult.class)))
                .addNode(ProcessorNodeBuilder.createWithId("scene-match-sink")
                        .withComponentId("scene-match-sinker")
                        .withParallelism(1)
                        .withOutputType(Event.class)
                        .from("scene-event-source")
                )
                .build();
    }

    private static ProcessorGraph thenGraph(){
        return ProcessorGraphBuilder.createWithId("then")
                .addNode(ProcessorNodeBuilder.createWithId("matched-scene-source")
                        .withComponentId("matched-scene-producer")
                        .withParallelism(1)
                        .withOutputType(SceneMatchedResult.class)
                )
                .addNode(ProcessorNodeBuilder.createWithId("scene-handler")
                        .withComponentId("scene-selector")
                        .withParallelism(1)
                        .withOutputType(ThenRequest.class)
                        .from("matched-scene-source")
                )
                .addNode(ProcessorNodeBuilder.createWithId("then-handler")
                        .withComponentId("then-handler")
                        .withParallelism(1)
                        .withOutputType(Action.class)
                        .from("scene-handler")
                )
                .addNode(ProcessorNodeBuilder.createWithId("action-sink")
                        .withComponentId("action-sinker")
                        .withParallelism(1)
                        .withOutputType(Action.class)
                        .from("then-handler")
                )
                .build();
    }

    private outBoundActionCom(){

    }

    private static ProcessorGraph cacheScheduler(){
        return ProcessorGraphBuilder.createWithId("scheduler")
                .addNode(ProcessorNodeBuilder.createWithId("trigger-source")
                        .withComponentId("cron-trigger")
                        .withParallelism(1)
                        .withOutputType(String.class)
                )
                .addNode(ProcessorNodeBuilder.createWithId("cache-handler")
                        .withComponentId("cache-processor")
                        .withParallelism(2)
                        .withOutputType(Boolean.class)
                        .from("trigger-source")
                )
                .build();
    }

}
