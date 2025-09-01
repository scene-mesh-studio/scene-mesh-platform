package com.scene.mesh.foundation.impl.processor.flink;

import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import com.scene.mesh.foundation.spec.component.IComponentProvider;
import com.scene.mesh.foundation.spec.processor.IProcessor;
import com.scene.mesh.foundation.spec.processor.config.ProcessorNode;
import com.scene.mesh.foundation.impl.processor.ProcessActivateContext;
import com.scene.mesh.foundation.impl.processor.ProcessInput;
import com.scene.mesh.foundation.impl.processor.ProcessOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

@Slf4j
public class FlinkProcessProducer extends RichSourceFunction implements IFlinkProcessorAgent {

    private final boolean asProducer;
    private final ProcessorNode processorNode;
    private final IComponentProvider componentProvider;
    private IProcessor processor;
    private boolean willCancel;
    private Class outputType;
    private String env;

    public FlinkProcessProducer(ProcessorNode processorNode, IComponentProvider componentProvider) {
        this.processorNode = processorNode;
        this.componentProvider = componentProvider;
        this.willCancel = false;
        this.asProducer = true;
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        SourceContextProxyCollector proxyCollector = new SourceContextProxyCollector(sourceContext);
        while (!this.willCancel) {

            ProcessInput processInput = new ProcessInput();
            processInput.setInputObject(null);

            proxyCollector.reset();
            ProcessOutput processOutput = new ProcessOutput();
            processOutput.setCollector(proxyCollector);

            try {
                this.processor.process(processInput, processOutput);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!proxyCollector.hasObjectsCollectted()) {
                Thread.sleep(100);
            }

        }
    }

    @Override
    public void cancel() {
        this.willCancel = true;
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

    public void setEnv(String env) {
        this.env = env;
    }
}
