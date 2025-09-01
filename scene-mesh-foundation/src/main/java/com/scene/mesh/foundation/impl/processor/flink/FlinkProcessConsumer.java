package com.scene.mesh.foundation.impl.processor.flink;

import com.scene.mesh.foundation.spec.collector.ICollector;
import com.scene.mesh.foundation.spec.component.IComponentProvider;
import com.scene.mesh.foundation.spec.processor.IProcessor;
import com.scene.mesh.foundation.spec.processor.config.ProcessorNode;
import com.scene.mesh.foundation.impl.processor.ProcessActivateContext;
import com.scene.mesh.foundation.impl.processor.ProcessInput;
import com.scene.mesh.foundation.impl.processor.ProcessOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;


@Slf4j
public class FlinkProcessConsumer extends RichFlatMapFunction implements IFlinkProcessorAgent {

    private final boolean asProducer;
    private final ProcessorNode processorNode;
    private final IComponentProvider componentProvider;
    private IProcessor processor;
    private final boolean willCancel;
    private Class outputType;
    private String env;

    public FlinkProcessConsumer(ProcessorNode processorNode, IComponentProvider componentProvider) {
        this.processorNode = processorNode;
        this.componentProvider = componentProvider;
        this.willCancel = false;
        this.asProducer = false;
    }

    @Override
    public void flatMap(Object input, final Collector collector) throws Exception {
        ICollector proxyCollector = new ICollector() {
            @Override
            public void collect(Object object) {
                collector.collect(object);
            }
        };

        ProcessInput processInput = new ProcessInput();
        processInput.setInputObject(input);
        ProcessOutput processOutput = new ProcessOutput();
        processOutput.setCollector(proxyCollector);

        try {
            this.processor.process(processInput, processOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void open() throws Exception {
        this.processor = (IProcessor) this.componentProvider.getComponent(this.processorNode.getComponentId());
        ProcessActivateContext activateContext = new ProcessActivateContext();
        this.processor.activate(activateContext);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        System.setProperty("execute.env", env);
        super.open(parameters);
        this.open();
    }

    @Override
    public boolean isAsProducer() {
        return this.asProducer;
    }

    @Override
    public ProcessorNode getProcessorNode() {
        return this.processorNode;
    }

    @Override
    public Class getOutputType() {
        return this.processorNode.getOutputType() == null ? Object.class : this.processorNode.getOutputType();
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
