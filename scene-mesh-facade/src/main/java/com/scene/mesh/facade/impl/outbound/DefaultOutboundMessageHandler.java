package com.scene.mesh.facade.impl.outbound;

import com.scene.mesh.facade.spec.outbound.IOutboundMessageHandler;
import com.scene.mesh.facade.spec.outbound.OutboundMessage;
import com.scene.mesh.facade.spec.protocol.IProtocolService;
import com.scene.mesh.facade.spec.protocol.IProtocolServiceManager;
import com.scene.mesh.facade.spec.protocol.TerminalProtocolStateManager;
import com.scene.mesh.model.protocol.ProtocolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultOutboundMessageHandler implements IOutboundMessageHandler {

    private final TerminalProtocolStateManager terminalProtocolStateManager;

    private final IProtocolServiceManager protocolServiceManager;

    public DefaultOutboundMessageHandler(TerminalProtocolStateManager terminalProtocolStateManager, IProtocolServiceManager protocolServiceManager) {
        this.terminalProtocolStateManager = terminalProtocolStateManager;
        this.protocolServiceManager = protocolServiceManager;
    }

    @Override
    public void handle(OutboundMessage outboundMessage) {
        String terminalId = outboundMessage.getTerminalId();
        ProtocolType currentProtocolType = this.terminalProtocolStateManager.getProtocolState(terminalId);
        if (currentProtocolType == null) {
            log.info("Unable to obtain the current terminal protocol. The terminal may have disconnected. - terminalId={}", terminalId);
            return;
        }
        IProtocolService protocolService = this.protocolServiceManager.getProtocolService(currentProtocolType);
        if (protocolService == null) {
            throw new RuntimeException("Can't find protocol service for protocol type: websocket");
        }
        protocolService.send(outboundMessage);
        log.info("Send outbound message to terminal protocol state: {}", outboundMessage);
    }

}
