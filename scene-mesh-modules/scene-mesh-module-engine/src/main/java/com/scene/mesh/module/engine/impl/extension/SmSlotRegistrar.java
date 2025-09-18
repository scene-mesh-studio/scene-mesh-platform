package com.scene.mesh.module.engine.impl.extension;

import com.scene.mesh.module.api.calculate.IParameterCalculator;
import com.scene.mesh.module.api.validator.IParameterValidator;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionManager;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionSlot;
import lombok.extern.slf4j.Slf4j;

/**
 * 扩展槽注册器
 */
@Slf4j
public class SmSlotRegistrar {

    /**
     * Calculate Slot 的标识符
     */
    public static final String CALCULATE_SLOT_ID = "calculate";

    /**
     * Validator Slot 的标识符
     */
    public static final String VALIDATOR_SLOT_ID = "validator";

    /**
     * 注册所有内置扩展槽
     */
    public static int registerAllBuiltinSlots(ISmExtensionManager extensionManager) {
        if (extensionManager == null) {
            log.error("Extension manager cannot be null");
            return 0;
        }

        int successCount = 0;
        
        // 注册 calculate 扩展槽
        if (registerCalculateSlot(extensionManager)) {
            successCount++;
        }
        
        // 注册 validator 扩展槽
        if (registerValidatorSlot(extensionManager)) {
            successCount++;
        }
        
        // 未来可以在这里添加更多扩展槽注册
        // if (registerRenderSlot(extensionManager)) {
        //     successCount++;
        // }
        // if (registerStorageSlot(extensionManager)) {
        //     successCount++;
        // }

        log.info("Registered {}/{} builtin extension slots", successCount, 2);
        return successCount;
    }

    /**
     * 注销所有内置扩展槽
     * @param extensionManager 扩展管理器
     * @return 注销成功的扩展槽数量
     */
    public static int unregisterAllBuiltinSlots(ISmExtensionManager extensionManager) {
        if (extensionManager == null) {
            log.error("Extension manager cannot be null");
            return 0;
        }

        int successCount = 0;
        
        // 注销 calculate 扩展槽
        if (unregisterCalculateSlot(extensionManager)) {
            successCount++;
        }
        
        // 注销 validator 扩展槽
        if (unregisterValidatorSlot(extensionManager)) {
            successCount++;
        }
        
        // 未来可以在这里添加更多扩展槽注销
        // if (unregisterRenderSlot(extensionManager)) {
        //     successCount++;
        // }
        // if (unregisterStorageSlot(extensionManager)) {
        //     successCount++;
        // }

        log.info("Unregistered {}/{} builtin extension slots", successCount, 2);
        return successCount;
    }

    /**
     * 注册 calculate 扩展槽
     * @param extensionManager 扩展管理器
     * @return 是否注册成功
     */
    private static boolean registerCalculateSlot(ISmExtensionManager extensionManager) {
        try {
            // 创建 calculate 扩展槽
            ISmExtensionSlot calculateSlot = new SmExtensionSlot(
                CALCULATE_SLOT_ID, 
                IParameterCalculator.class
            );

            // 注册到扩展管理器
            boolean success = extensionManager.registerExtensionSlot(calculateSlot);
            
            if (success) {
                log.info("Successfully registered calculate slot with interface: {}", 
                    IParameterCalculator.class.getSimpleName());
            } else {
                log.warn("Failed to register calculate slot");
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error registering calculate slot", e);
            return false;
        }
    }

    /**
     * 注销 calculate 扩展槽
     * @param extensionManager 扩展管理器
     * @return 是否注销成功
     */
    private static boolean unregisterCalculateSlot(ISmExtensionManager extensionManager) {
        try {
            boolean success = extensionManager.unregisterExtensionSlot(CALCULATE_SLOT_ID);
            
            if (success) {
                log.info("Successfully unregistered calculate slot");
            } else {
                log.warn("Failed to unregister calculate slot");
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error unregistering calculate slot", e);
            return false;
        }
    }

    /**
     * 注册 validator 扩展槽
     * @param extensionManager 扩展管理器
     * @return 是否注册成功
     */
    private static boolean registerValidatorSlot(ISmExtensionManager extensionManager) {
        try {
            // 创建 validator 扩展槽
            ISmExtensionSlot validatorSlot = new SmExtensionSlot(
                VALIDATOR_SLOT_ID, 
                IParameterValidator.class
            );

            // 注册到扩展管理器
            boolean success = extensionManager.registerExtensionSlot(validatorSlot);
            
            if (success) {
                log.info("Successfully registered validator slot with interface: {}", 
                    IParameterValidator.class.getSimpleName());
            } else {
                log.warn("Failed to register validator slot");
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error registering validator slot", e);
            return false;
        }
    }

    /**
     * 注销 validator 扩展槽
     * @param extensionManager 扩展管理器
     * @return 是否注销成功
     */
    private static boolean unregisterValidatorSlot(ISmExtensionManager extensionManager) {
        try {
            boolean success = extensionManager.unregisterExtensionSlot(VALIDATOR_SLOT_ID);
            
            if (success) {
                log.info("Successfully unregistered validator slot");
            } else {
                log.warn("Failed to unregister validator slot");
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error unregistering validator slot", e);
            return false;
        }
    }

    /**
     * 检查 calculate 扩展槽是否已注册
     * @param extensionManager 扩展管理器
     * @return 是否已注册
     */
    public static boolean isCalculateSlotRegistered(ISmExtensionManager extensionManager) {
        if (extensionManager == null) {
            return false;
        }
        
        return extensionManager.findExtensionSlot(CALCULATE_SLOT_ID) != null;
    }

    /**
     * 检查 validator 扩展槽是否已注册
     * @param extensionManager 扩展管理器
     * @return 是否已注册
     */
    public static boolean isValidatorSlotRegistered(ISmExtensionManager extensionManager) {
        if (extensionManager == null) {
            return false;
        }
        
        return extensionManager.findExtensionSlot(VALIDATOR_SLOT_ID) != null;
    }

}
