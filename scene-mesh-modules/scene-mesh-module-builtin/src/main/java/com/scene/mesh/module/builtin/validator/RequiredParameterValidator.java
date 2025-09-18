package com.scene.mesh.module.builtin.validator;

import com.scene.mesh.module.api.annotation.ExtensionPlugin;
import com.scene.mesh.module.api.validator.BaseParameterValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 必需参数验证器
 * 验证指定的参数是否存在于payload中
 */
@Slf4j
@ExtensionPlugin(
    slotId = "validator",
    pluginId = "required",
    name = "Required Parameter Validator",
    description = "验证指定的参数是否存在于payload中",
    version = "1.0.0",
    priority = 100
)
public class RequiredParameterValidator extends BaseParameterValidator {

    @Override
    public String getValidatorType() {
        return "required";
    }

    @Override
    protected boolean doValidate(Map<String, Object> payload) {
        try {
            // 获取需要验证的必需参数列表
            String[] requiredParams = getRequiredParameters();
            
            if (requiredParams == null || requiredParams.length == 0) {
                setErrorMessage("No required parameters specified");
                return false;
            }

            // 检查每个必需参数
            for (String param : requiredParams) {
                if (!payload.containsKey(param)) {
                    setErrorMessage("Required parameter '" + param + "' is missing");
                    log.warn("Required parameter validation failed: missing '{}'", param);
                    return false;
                }
                
                Object value = payload.get(param);
                if (value == null) {
                    setErrorMessage("Required parameter '" + param + "' cannot be null");
                    log.warn("Required parameter validation failed: '{}' is null", param);
                    return false;
                }
                
                // 检查字符串参数是否为空
                if (value instanceof String && ((String) value).trim().isEmpty()) {
                    setErrorMessage("Required parameter '" + param + "' cannot be empty");
                    log.warn("Required parameter validation failed: '{}' is empty", param);
                    return false;
                }
            }

            log.debug("Required parameter validation passed for {} parameters", requiredParams.length);
            return true;

        } catch (Exception e) {
            setErrorMessage("Required parameter validation error: " + e.getMessage());
            log.error("Required parameter validation failed with exception", e);
            return false;
        }
    }

    /**
     * 获取需要验证的必需参数列表
     * 子类可以重写此方法来指定特定的必需参数
     * @return 必需参数名称数组
     */
    protected String[] getRequiredParameters() {
        // 默认验证常见的必需参数
        return new String[]{"text", "audioUrl"};
    }

    /**
     * 创建指定必需参数的验证器
     * @param requiredParams 必需参数列表
     * @return 验证器实例
     */
    public static RequiredParameterValidator forParameters(String... requiredParams) {
        return new RequiredParameterValidator() {
            @Override
            protected String[] getRequiredParameters() {
                return requiredParams;
            }
        };
    }
}
