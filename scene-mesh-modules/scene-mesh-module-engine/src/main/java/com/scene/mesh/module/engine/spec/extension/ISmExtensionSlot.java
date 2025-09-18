package com.scene.mesh.module.engine.spec.extension;

/**
 * 扩展槽
 */
public interface ISmExtensionSlot {
    /**
     * ID
     */
    String getId();

    /**
     * 扩展点接口类型
     */
    Class<?> getSlotInterfaceClass();

    /**
     * 扩展点接口定义
     */
    String getSlotInterfaceDefinition();

    /**
     * 扩展点的方法签名
     */
    String[] getSlotInterfaceMethodSignatures();
}
