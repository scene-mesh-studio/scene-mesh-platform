package com.scene.mesh.module.builtin.validator;

import com.scene.mesh.module.api.annotation.ExtensionPlugin;
import com.scene.mesh.module.api.validator.BaseParameterValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 格式验证器
 * 验证参数是否符合指定的格式要求
 */
@Slf4j
@ExtensionPlugin(
    slotId = "validator",
    pluginId = "format",
    name = "Format Validator",
    description = "验证参数是否符合指定的格式要求",
    version = "1.0.0",
    priority = 100
)
public class FormatValidator extends BaseParameterValidator {

    @Override
    public String getValidatorType() {
        return "format";
    }

    @Override
    protected boolean doValidate(Map<String, Object> payload) {
        try {
            // 验证URL格式
            if (!validateUrlFormat(payload)) {
                return false;
            }

            // 验证邮箱格式
            if (!validateEmailFormat(payload)) {
                return false;
            }

            // 验证语言代码格式
            if (!validateLanguageFormat(payload)) {
                return false;
            }

            // 验证数值范围
            if (!validateNumericRange(payload)) {
                return false;
            }

            log.debug("Format validation passed");
            return true;

        } catch (Exception e) {
            setErrorMessage("Format validation error: " + e.getMessage());
            log.error("Format validation failed with exception", e);
            return false;
        }
    }

    /**
     * 验证URL格式
     */
    private boolean validateUrlFormat(Map<String, Object> payload) {
        String audioUrl = (String) payload.get("audioUrl");
        if (audioUrl != null && !audioUrl.trim().isEmpty()) {
            if (!isValidUrl(audioUrl)) {
                setErrorMessage("Invalid URL format: " + audioUrl);
                log.warn("URL format validation failed: {}", audioUrl);
                return false;
            }
        }
        return true;
    }

    /**
     * 验证邮箱格式
     */
    private boolean validateEmailFormat(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        if (email != null && !email.trim().isEmpty()) {
            if (!isValidEmail(email)) {
                setErrorMessage("Invalid email format: " + email);
                log.warn("Email format validation failed: {}", email);
                return false;
            }
        }
        return true;
    }

    /**
     * 验证语言代码格式
     */
    private boolean validateLanguageFormat(Map<String, Object> payload) {
        String language = (String) payload.get("language");
        if (language != null && !language.trim().isEmpty()) {
            if (!isValidLanguageCode(language)) {
                setErrorMessage("Invalid language code format: " + language);
                log.warn("Language code format validation failed: {}", language);
                return false;
            }
        }
        return true;
    }

    /**
     * 验证数值范围
     */
    private boolean validateNumericRange(Map<String, Object> payload) {
        // 验证速度范围 (0.1 - 3.0)
        Double speed = (Double) payload.get("speed");
        if (speed != null && (speed < 0.1 || speed > 3.0)) {
            setErrorMessage("Speed must be between 0.1 and 3.0, got: " + speed);
            log.warn("Speed range validation failed: {}", speed);
            return false;
        }

        // 验证音调范围 (0.1 - 2.0)
        Double pitch = (Double) payload.get("pitch");
        if (pitch != null && (pitch < 0.1 || pitch > 2.0)) {
            setErrorMessage("Pitch must be between 0.1 and 2.0, got: " + pitch);
            log.warn("Pitch range validation failed: {}", pitch);
            return false;
        }

        // 验证音量范围 (0.0 - 1.0)
        Double volume = (Double) payload.get("volume");
        if (volume != null && (volume < 0.0 || volume > 1.0)) {
            setErrorMessage("Volume must be between 0.0 and 1.0, got: " + volume);
            log.warn("Volume range validation failed: {}", volume);
            return false;
        }

        return true;
    }

    /**
     * 验证URL格式
     */
    private boolean isValidUrl(String url) {
        try {
            String urlPattern = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
            return Pattern.matches(urlPattern, url);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        try {
            String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            return Pattern.matches(emailPattern, email);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证语言代码格式 (ISO 639-1)
     */
    private boolean isValidLanguageCode(String language) {
        try {
            String languagePattern = "^[a-z]{2}(-[A-Z]{2})?$";
            return Pattern.matches(languagePattern, language);
        } catch (Exception e) {
            return false;
        }
    }
}
