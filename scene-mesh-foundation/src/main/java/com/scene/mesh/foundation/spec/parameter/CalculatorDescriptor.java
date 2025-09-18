package com.scene.mesh.foundation.spec.parameter;

import lombok.Data;

/**
 * 字段计算描述
 */
@Data
public class CalculatorDescriptor {

    private String calculatorType;

    private String sourceField;

    public CalculatorDescriptor(String calculatorType, String sourceField) {
        this.calculatorType = calculatorType;
        this.sourceField = sourceField;
    }

    public CalculatorDescriptor() {
    }
}
