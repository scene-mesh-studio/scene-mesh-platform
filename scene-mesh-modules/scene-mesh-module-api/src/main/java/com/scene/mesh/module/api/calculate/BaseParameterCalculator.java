package com.scene.mesh.module.api.calculate;

import java.util.Map;

/**
 * 参数计算器基础实现类
 * 提供通用的计算流程和验证逻辑
 */
public abstract class BaseParameterCalculator implements IParameterCalculator {

    @Override
    public void calculate(String sourceField, Map<String, Object> payload) {
        // 通用验证逻辑
        validatePayload(payload);
        
        // 执行具体计算
        doCalculate(sourceField, payload);
    }

    /**
     * 验证输入参数
     */
    protected void validatePayload(Map<String, Object> payload) {
        //TODO
    }

    /**
     * 执行具体的计算逻辑
     */
    protected abstract void doCalculate(String sourceField, Map<String, Object> payload);
}
