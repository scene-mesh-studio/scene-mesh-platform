package com.scene.mesh.foundation.impl.processor.flink;

import com.scene.mesh.foundation.impl.processor.flink.cep.discover.IRuleDiscoverer;
import com.scene.mesh.foundation.impl.processor.flink.cep.discover.ScenePeriodicWhenDiscovererFactory;
import com.scene.mesh.foundation.spec.component.IComponentProvider;
import com.scene.mesh.foundation.spec.processor.IProcessActuator;
import com.scene.mesh.foundation.spec.processor.config.CepModeDescriptor;
import com.scene.mesh.foundation.spec.processor.config.ProcessorGraph;
import com.scene.mesh.foundation.spec.processor.config.ProcessorLinker;
import com.scene.mesh.foundation.spec.processor.config.ProcessorNode;
import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEPUtils;
import org.apache.flink.cep.TimeBehaviour;
import org.apache.flink.configuration.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.*;

@Slf4j
public class FlinkProcessActuator implements IProcessActuator {

    private ProcessorGraph processorGraph;
    private String[] processorArgs;
    private boolean launched;
    private final Map<String, FlinkProcessorChainNode> chainNodeMap;
    private final IComponentProvider componentProvider;
    private final Configuration configuration;

    public FlinkProcessActuator(IComponentProvider componentProvider, Configuration configuration) {
        this.componentProvider = componentProvider;
        this.chainNodeMap = new HashMap<>();
        this.configuration = configuration;
    }

    @Override
    public void initialize(ProcessorGraph graph, String... args) {
        this.processorGraph = graph;
        this.processorArgs = args;
        this.launched = false;
    }

    @Override
    public void launch() throws Exception {
        this.launched = false;
        if (this.processorGraph == null) return;


        List<ProcessorNode> producerNodes = this.processorGraph.getProducerNodes();
        if (producerNodes == null || producerNodes.isEmpty()) return;

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        env.disableOperatorChaining();

        for (ProcessorNode pn : this.processorGraph.getNodes()) {
            if (this.processorGraph.isProducerNode(pn.getId())) {
                FlinkProcessProducer producer = new FlinkProcessProducer(pn, this.componentProvider);
                producer.setEnv(System.getProperty("execute.env"));
                FlinkProcessorChainNode chainNode = new FlinkProcessorChainNode(producer);
                this.chainNodeMap.put(pn.getId(), chainNode);
            } else {
                FlinkProcessConsumer consumer = new FlinkProcessConsumer(pn, this.componentProvider);
                consumer.setEnv(System.getProperty("execute.env"));
                FlinkProcessorChainNode chainNode = new FlinkProcessorChainNode(consumer);
                this.chainNodeMap.put(pn.getId(), chainNode);
            }
        }

        for (String chainNodeId : this.chainNodeMap.keySet()) {
            this.setupChainObjectNode(chainNodeId, env);
        }

        System.out.println(">>>>>>>>>> " + env.getExecutionPlan());

        env.execute("Execute graph plan : GraphId[" + this.processorGraph.getGraphId() + "]");
    }

    private DataStream setupChainObjectNode(String nodeId, StreamExecutionEnvironment env) {
        FlinkProcessorChainNode chainNode = this.chainNodeMap.get(nodeId);
        if (chainNode == null) return null;

        if (chainNode.getChainObject() != null) {
            return chainNode.getChainObject();
        }

        List<ProcessorLinker> linkers = this.processorGraph.findLinkersByNodeId(nodeId, false); //find upstreams
        if (linkers.size() == 0 && processorGraph.isProducerNode(nodeId)) {
            DataStream dataStreamSource = env
                    .addSource((FlinkProcessProducer) chainNode.getAgent())
                    .returns(chainNode.getAgent().getOutputType())
                    .name(nodeId)
                    .setParallelism(chainNode.getAgent().getProcessorNode().getParallelism())
                    .shuffle();
            //如果有 cep 处理，在 source 后添加 cep table
            if (this.processorGraph.isEnableCepMode()) {
                CepModeDescriptor descriptor = this.processorGraph.getCepModeDescriptor();

                //校验 cep 分区键
                if (descriptor.getKeyed() == null || descriptor.getKeyed().length != 1) {
                    throw new RuntimeException("Cep 模式 - keyed 设置无效.");
                }

                // source stream 分区处理
                KeyedStream keyedStream = dataStreamSource.keyBy(new KeySelector<Object, String>() {
                    @Override
                    public String getKey(Object obj) throws Exception {
                        Map<String, Object> objectMap = SimpleObjectHelper.obj2Map(obj);
                        String[] keyeds = descriptor.getKeyed();
                        return objectMap.get(keyeds[0]).toString();
                    }
                });

                //设置定时 cep 规则
//                SingleOutputStreamOperator<Object> cepResultStream = CEPUtils.dynamicCepRules(keyedStream,
//                        new JdbcPeriodicRuleDiscovererFactory(
//                                JdbcConnectorOptions.builder()
//                                        .setTableName(descriptor.getRuleSource())
//                                        .setDriverName(descriptor.getDriverName())
//                                        .setDBUrl(descriptor.getDatabaseUrl())
//                                        .setUsername(descriptor.getUsername())
//                                        .setPassword(descriptor.getPassword())
//                                        .build(),
//                                3,
//                                "cep",
//                                Collections.emptyList(),
//                                descriptor.getPeriod().toMillis()),
//                        TimeBehaviour.ProcessingTime,
//                        descriptor.getCepMatchedResultType(),
//                        "event-cep",
//                        "/",
//                        descriptor.getParallelism(),
//                        false
//                );

                SingleOutputStreamOperator<Object> cepResultStream = CEPUtils.dynamicCepRules(keyedStream,
                        new ScenePeriodicWhenDiscovererFactory(
                                Collections.emptyList(),
                                descriptor.getPeriod().toMillis(),
                                descriptor.getDiscoverComponent(),
                                componentProvider),
                        TimeBehaviour.ProcessingTime,
                        descriptor.getCepMatchedResultType(),
                        "event-cep",
                        "/",
                        descriptor.getParallelism(),
                        false
                );

                chainNode.setChainObject(cepResultStream);
                return cepResultStream;
            } else {
                chainNode.setChainObject(dataStreamSource);
                return dataStreamSource;
            }
        } else if (linkers.size() > 0 && !processorGraph.isProducerNode(nodeId)) {
            List<DataStream> streams = new ArrayList<>();
            for (ProcessorLinker linker : linkers) {
                DataStream dataStream = this.setupChainObjectNode(linker.getFromNodeId(), env);
                if (dataStream != null) {
                    streams.add(dataStream);
                }
            }
            if (streams.size() == 1) {
                DataStream ds = streams.get(0)
                        .flatMap((FlinkProcessConsumer) chainNode.getAgent())
                        .returns(chainNode.getAgent().getOutputType())
                        .setParallelism(chainNode.getAgent().getProcessorNode().getParallelism())
                        .name(nodeId);
                chainNode.setChainObject(ds);
                return ds;
            } else if (streams.size() > 1) {
                DataStream ds = streams.get(0)
                        .union(streams.subList(1, streams.size()).toArray(new DataStream[streams.size() - 1]))
                        .flatMap((FlinkProcessConsumer) chainNode.getAgent())
                        .returns(chainNode.getAgent().getOutputType())
                        .setParallelism(chainNode.getAgent().getProcessorNode().getParallelism())
                        .name(nodeId);
                chainNode.setChainObject(ds);
                return ds;
            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public boolean isLaunched() {
        return this.launched;
    }

    public class FlinkProcessorChainNode {

        private final IFlinkProcessorAgent agent;
        private DataStream chainObject;

        public FlinkProcessorChainNode(IFlinkProcessorAgent agent) {
            this.agent = agent;
        }

        public IFlinkProcessorAgent getAgent() {
            return agent;
        }

        public DataStream getChainObject() {
            return chainObject;
        }

        public void setChainObject(DataStream chainObject) {
            this.chainObject = chainObject;
        }
    }
}
