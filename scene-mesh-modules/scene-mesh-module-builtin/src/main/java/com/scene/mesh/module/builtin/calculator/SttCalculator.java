package com.scene.mesh.module.builtin.calculator;

import com.scene.mesh.module.api.annotation.ExtensionPlugin;
import com.scene.mesh.module.api.calculate.BaseParameterCalculator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * STT (Speech-to-Text) 计算器
 * 将语音转换为文本参数
 */
@Slf4j
@ExtensionPlugin(
    slotId = "calculate",
    pluginId = "STT",
    name = "STT Calculator",
    description = "将语音转换为文本参数",
    version = "1.0.0"
)
public class SttCalculator extends BaseParameterCalculator {

    @Override
    protected void doCalculate(String sourceField, Map<String, Object> payload) {
        System.out.println("STT 执行：" + sourceField);
    }
}
