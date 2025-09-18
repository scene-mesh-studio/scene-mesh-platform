package com.scene.mesh.facade.impl.inbound;

import com.scene.mesh.foundation.spec.parameter.CalculatorDescriptor;
import com.scene.mesh.foundation.spec.parameter.MetaParameterDescriptor;
import com.scene.mesh.foundation.spec.parameter.MetaParameterDescriptorCollection;
import com.scene.mesh.module.api.calculate.IParameterCalculator;
import com.scene.mesh.model.event.Event;
import com.scene.mesh.model.event.IMetaEvent;
import com.scene.mesh.module.engine.impl.extension.SmExtensionInvoker;
import com.scene.mesh.module.engine.impl.extension.SmSlotRegistrar;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionInvoker;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionManager;
import com.scene.mesh.service.spec.event.IMetaEventService;

import java.util.List;
import java.util.Map;

public class ComputableFieldCalculator extends BaseInboundMessageInterceptor {

    private final IMetaEventService metaEventService;
    private final ISmExtensionInvoker extensionInvoker;

    public ComputableFieldCalculator(IMetaEventService metaEventService, ISmExtensionInvoker extensionInvoker) {
        this.metaEventService = metaEventService;
        this.extensionInvoker = extensionInvoker;
    }

    @Override
    protected void doIntercept(InboundMessageRequest request, InboundMessageResponse response) {
        Event event = (Event) response.getPropVal("event");
        String metaEventId = event.getType();
        IMetaEvent metaEvent = this.metaEventService.getIMetaEvent(metaEventId);
        Map<String, Object> payload = event.getPayload();

        MetaParameterDescriptorCollection collection = metaEvent.getParameterCollection();
        for (MetaParameterDescriptor parameterDescriptor : collection.getParameterDescriptors()) {
            CalculatorDescriptor calculatorDescriptor = parameterDescriptor.getCalculatorDescriptor();
            if (calculatorDescriptor == null) continue;

            // 将CalculateType枚举转换为String类型
            String calculatorType = calculatorDescriptor.getCalculatorType();
            String sourceField = calculatorDescriptor.getSourceField();

            // 直接调用扩展插件的calculate方法
            extensionInvoker.invokeExtension(
                    SmSlotRegistrar.CALCULATE_SLOT_ID,
                    calculatorType,
                    "calculate",
                    sourceField,
                    event.getPayload()
            );
        }
        event.setPayload(payload);
    }

    @Override
    public String getName() {
        return "ComputableFieldCalculator";
    }
}
