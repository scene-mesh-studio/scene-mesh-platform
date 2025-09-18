package com.scene.mesh.module.api.calculate;

import java.util.Map;

/**
 * 参数计算器接口
 */
public interface IParameterCalculator {
    /**
     * 执行参数计算
     * @param sourceField 来源字段
     * @param payload 输入参数
     */
    void calculate(String sourceField, Map<String, Object> payload);
}
