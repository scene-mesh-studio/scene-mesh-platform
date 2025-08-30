package com.scene.mesh.service.impl.event;

import com.scene.mesh.foundation.spec.parameter.MetaParameterDescriptor;
import com.scene.mesh.foundation.spec.parameter.data.calculate.BaseParameterCalculator;
import com.scene.mesh.service.spec.speech.ISpeechService;
import org.springframework.stereotype.Component;

import java.util.Map;

public class SttParameterCalculator extends BaseParameterCalculator {

    private final ISpeechService speechService;

    public SttParameterCalculator(ISpeechService speechService) {
        this.speechService = speechService;
    }

    @Override
    public CalculateType getCalculateType() {
        return CalculateType.STT;
    }

    @Override
    public void calculate(String terminalId, Map<String,Object> payload, MetaParameterDescriptor calculatedField) {
        if (payload.get("audio") == null || !(payload.get("audio") instanceof String base64Audio)) {
            throw new RuntimeException("Can not find calculate audio parameter in payload.");
        }

        String text = this.speechService.stt(terminalId, base64Audio);

        payload.put(calculatedField.getName(), text);
    }
}
