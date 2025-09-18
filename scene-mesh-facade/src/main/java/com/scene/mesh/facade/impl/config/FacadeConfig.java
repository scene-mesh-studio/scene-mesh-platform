package com.scene.mesh.facade.impl.config;

import com.scene.mesh.facade.impl.common.DefaultTerminalAuthenticator;
import com.scene.mesh.facade.impl.inbound.ComputableFieldCalculator;
import com.scene.mesh.facade.spec.common.ITerminalAuthenticator;
import com.scene.mesh.facade.spec.inboud.InboundMessageInterceptor;
import com.scene.mesh.facade.impl.inbound.MessageLegalityChecker;
import com.scene.mesh.facade.impl.inbound.MessageToEventConvertor;
import com.scene.mesh.facade.spec.protocol.TerminalProtocolStateManager;
import com.scene.mesh.foundation.spec.message.MessageTopic;
import com.scene.mesh.module.engine.impl.SmModuleManager;
import com.scene.mesh.module.engine.impl.extension.SmExtensionInvoker;
import com.scene.mesh.module.engine.impl.extension.SmExtensionManager;
import com.scene.mesh.module.engine.spec.ISmModuleLoader;
import com.scene.mesh.module.engine.spec.ISmModuleManager;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionInvoker;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionManager;
import com.scene.mesh.service.spec.event.IMetaEventService;
import com.scene.mesh.service.spec.product.IProductService;
import com.scene.mesh.service.spec.terminal.ITerminalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class FacadeConfig {

    @Value("${topic.inbound-event}")
    private String inboundEventTopic;

    @Value("${topic.outbound-action}")
    private String outboundActionTopic;

    @Bean
    public List<InboundMessageInterceptor> messageInterceptors(
            IMetaEventService metaEventService,
            ISmExtensionInvoker extensionInvoker) {
        List<InboundMessageInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new MessageLegalityChecker(metaEventService));
        interceptors.add(new MessageToEventConvertor());
        interceptors.add(new ComputableFieldCalculator(metaEventService, extensionInvoker));
        return interceptors;
    }

    @Bean
    public ISmExtensionManager smExtensionManager(){
        return new SmExtensionManager();
    }

    @Bean
    public ISmModuleManager smModuleManager(ISmExtensionManager smExtensionManager){
        ISmModuleManager smModuleManager = new SmModuleManager(smExtensionManager);
        try {
            smModuleManager.loadModule("scene-mesh-builtin","/Users/fang/develop/project/org/scene-mesh-platform/scene-mesh-modules/scene-mesh-module-builtin/target/scene-mesh-module-builtin-1.0.0-SNAPSHOT.jar");
        } catch (ISmModuleLoader.ModuleLoadException e) {
            throw new RuntimeException(e);
        }
        return smModuleManager;
    }

    @Bean
    public ISmExtensionInvoker extensionInvoker(ISmExtensionManager smExtensionManager){
        return new SmExtensionInvoker(smExtensionManager);
    }

    @Bean
    public MessageTopic inboundEventTopic(){
        return new MessageTopic(inboundEventTopic);
    }

    @Bean
    public MessageTopic outboundActionTopic(){
        return new MessageTopic(outboundActionTopic);
    }

    @Bean
    public TerminalProtocolStateManager terminalProtocolStateManager(){
        return new TerminalProtocolStateManager();
    }

    @Bean
    public ITerminalAuthenticator terminalAuthenticator(ITerminalService terminalService, IProductService productService, TerminalProtocolStateManager terminalProtocolStateManager){
        return new DefaultTerminalAuthenticator(terminalService,productService,terminalProtocolStateManager);
    }
}
