package com.scene.mesh.foundation.spec.module.extension;

/**
 * 扩展插件
 */
public interface ISmExtensionPlugin {
    /**
     * ID
     */
    String getId();

    /**
     * 名称
     */
    String  getName();

    /**
     * 扩展槽 ID
     */
    String getExtensionSlotId();

    /**
     * 所属 ModuleId
     */
    String getModuleId();

    /**
     * 扩展实现的实例
     */
    Object getPluginInstance();

    /**
     * 扩展插件负责的接口类型
     */
    Class<?> getPluginInterfaceClass();
}
