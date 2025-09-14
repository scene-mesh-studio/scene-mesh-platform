package com.scene.mesh.facade.impl.common;

import com.scene.mesh.facade.spec.common.IMessageExchanger;
import com.scene.mesh.facade.spec.outbound.IOutboundMessageHandler;
import com.scene.mesh.facade.spec.outbound.OutboundMessage;
import com.scene.mesh.facade.spec.outbound.OutboundMessageType;
import com.scene.mesh.foundation.spec.message.IMessageProducer;
import com.scene.mesh.foundation.spec.message.MessageTopic;
import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import com.scene.mesh.model.action.Action;
import com.scene.mesh.model.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultMessageExchanger implements IMessageExchanger {

    private final IOutboundMessageHandler outboundMessageHandler;

    private final IMessageProducer messageProducer;

    private final MessageTopic inboundEventTopic;

    public DefaultMessageExchanger(IOutboundMessageHandler outboundMessageHandler, IMessageProducer messageProducer, MessageTopic inboundEventTopic) {
        this.outboundMessageHandler = outboundMessageHandler;
        this.messageProducer = messageProducer;
        this.inboundEventTopic = inboundEventTopic;
    }

    @Override
    public void handleInboundEvent(Event event) {
        log.debug("handle inbound event: {}", event.toString());
        try {
            this.messageProducer.send(inboundEventTopic, event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleOutboundAction(Action action) {
        log.info("handle outbound action: {}", SimpleObjectHelper.objectData2json(action));
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.setTerminalId(action.getTerminalId());
        outboundMessage.setMessage(SimpleObjectHelper.objectData2json(action));
        outboundMessage.setOutboundMessageType(OutboundMessageType.ACTION);
        this.outboundMessageHandler.handle(outboundMessage);
    }

    @Override
    public void handleErrorOutboundMessage(OutboundMessage outboundMessage) {
        outboundMessage.setOutboundMessageType(OutboundMessageType.ERROR);
        this.outboundMessageHandler.handle(outboundMessage);
    }
}
