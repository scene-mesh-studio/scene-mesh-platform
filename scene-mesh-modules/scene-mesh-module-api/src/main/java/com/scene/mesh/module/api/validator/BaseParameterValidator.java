package com.scene.mesh.module.api.validator;

import java.util.Map;

/**
 * 参数验证器基础实现类
 * 提供通用的验证流程和错误处理逻辑
 */
public abstract class BaseParameterValidator implements IParameterValidator {

    protected String errorMessage = "Validation failed";

    @Override
    public boolean validate(Map<String, Object> payload) {
        try {
            // 通用验证逻辑
            validatePayload(payload);
            
            // 执行具体验证
            return doValidate(payload);
        } catch (Exception e) {
            this.errorMessage = e.getMessage();
            return false;
        }
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 验证输入参数的基本格式
     * @param payload 输入参数
     */
    protected void validatePayload(Map<String, Object> payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
    }

    /**
     * 执行具体的验证逻辑
     * 子类需要实现此方法
     * @param payload 输入参数
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    protected abstract boolean doValidate(Map<String, Object> payload);

    /**
     * 设置错误信息
     * @param errorMessage 错误信息
     */
    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
