package com.scene.mesh.engin.processor.then.operator;

import com.scene.mesh.foundation.spec.parameter.MetaParameterDescriptor;
import com.scene.mesh.foundation.spec.parameter.MetaParameterDescriptorCollection;
import com.scene.mesh.foundation.spec.processor.IProcessOutput;
import com.scene.mesh.model.event.Event;
import com.scene.mesh.model.event.IMetaEvent;
import com.scene.mesh.model.scene.Scene;
import com.scene.mesh.model.scene.WhenThen;
import com.scene.mesh.service.spec.ai.chat.IAgentService;
import com.scene.mesh.service.spec.ai.chat.IChatClientFactory;
import com.scene.mesh.service.impl.ai.chat.DefaultAgentService;
import com.scene.mesh.service.impl.ai.chat.DefaultChatClientFactory;
import com.scene.mesh.service.spec.ai.config.ILLmConfigService;
import com.scene.mesh.service.spec.ai.mcp.IToolsService;
import com.scene.mesh.service.spec.event.IMetaEventService;
import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent 代理类操作员
 */
public class AgentThenOperator implements IThenOperator{

    private final IAgentService agentService;

    private final IMetaEventService metaEventService;

    public AgentThenOperator(ILLmConfigService llmConfigService, IToolsService toolsService, IMetaEventService metaEventService) {
        IChatClientFactory chatClientFactory = new DefaultChatClientFactory(llmConfigService);
        this.agentService = new DefaultAgentService(chatClientFactory,toolsService);
        this.metaEventService = metaEventService;
    }

    @Override
    public String getOperatorType() {
        return "LLM_INFERENCE";
    }

    @Override
    public boolean process(String terminalId, Scene scene, WhenThen.Then then, List<Event> eventsInScene, IProcessOutput output) {
        // find input field in event
        List<Event> inputEvents = new ArrayList<>();
        for (Event event : eventsInScene) {
            Map<String,Object> payload = event.getPayload();
            Map<String,Object> inputPayload = new HashMap<>();
            IMetaEvent metaEvent = this.metaEventService.getIMetaEvent(event.getType());
            MetaParameterDescriptorCollection collection = metaEvent.getParameterCollection();
            for (MetaParameterDescriptor descriptor : collection.getParameterDescriptors()){
                if(descriptor.isAsInput()){
                    inputPayload.put(descriptor.getName(), payload.get(descriptor.getName()));
                }
            }
            Event inputEvent = SerializationUtils.clone(event);
            inputEvent.setPayload(inputPayload);
            inputEvents.add(inputEvent);
        }
        return this.agentService.callAgent(then,inputEvents);
    }

//    @Override
//    public void process(ThenRequest thenRequest, ThenResponse thenResponse) {
//        Operation operation = operationRequest.getOperation();
//        Agent agent = operation.getAgent();
//        List<Event> eventsInScene = operationRequest.getEventsInScene();
//
//        ChatResponse response = agentService.callAgent(agent,eventsInScene);
////        String assistantMessage = response.getResult().getOutput().getText();
////
////        Event event = new Event(OutputEventType.ASSISTANT.getName());
////        event.setProductId(operationRequest.getProductId());
////        event.setTerminalId(operationRequest.getTerminalId());
////        event.addPayloadEntry("assistantMessage", assistantMessage);
////        operationResponse.addEvent(event);
//    }
}
