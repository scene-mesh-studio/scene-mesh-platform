package com.scene.mesh.module.builtin.calculator;

import com.scene.mesh.module.api.annotation.ExtensionPlugin;
import com.scene.mesh.module.api.calculate.BaseParameterCalculator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * TTS (Text-to-Speech) 计算器
 * 将文本转换为语音参数
 */
@Slf4j
@ExtensionPlugin(
    slotId = "calculate",
    pluginId = "TTS",
    name = "TTS Calculator",
    description = "将文本转换为语音参数",
    version = "1.0.0",
    priority = 100
)
public class TtsCalculator extends BaseParameterCalculator {

    @Override
    protected void doCalculate(String sourceField, Map<String, Object> payload) {
        System.out.println("TTS 执行：" + sourceField);
    }
}
