package com.scene.mesh.module.api.validator;

import java.util.Map;

/**
 * 参数验证器接口
 */
public interface IParameterValidator {

    /**
     * 获取验证器类型标识符
     * @return 验证器类型，如 "required", "format", "range" 等
     */
    String getValidatorType();

    /**
     * 执行参数验证
     * @param payload 输入参数
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    boolean validate(Map<String, Object> payload);

    /**
     * 获取验证失败时的错误信息
     * @return 错误信息
     */
    String getErrorMessage();
}
