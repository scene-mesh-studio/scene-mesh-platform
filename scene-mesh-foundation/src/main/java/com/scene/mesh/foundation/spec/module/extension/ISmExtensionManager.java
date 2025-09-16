package com.scene.mesh.foundation.spec.module.extension;

import java.util.List;

/**
 * 扩展管理器
 */
public interface ISmExtensionManager {

    /**
     * 注册扩展槽
     */
    boolean registerExtensionSlot(ISmExtensionSlot extensionSlot);

    /**
     * 注销扩展槽
     */
    boolean unregisterExtensionSlot(String slotId);

    /**
     * 注册扩展插件
     */
    boolean registerExtensionPlugin(ISmExtensionPlugin extensionPlugin);

    /**
     * 注销扩展插件
     */
    boolean unregisterExtensionPlugin(String pluginId);

    /**
     * 查找扩展槽
     */
    ISmExtensionSlot findExtensionSlot(String slotId);

    /**
     * 获取所有扩展槽列表
     */
    List<ISmExtensionSlot> getAllExtensionSlots();

    /**
     * 查找扩展插件
     */
    ISmExtensionPlugin findExtensionPlugin(String pluginId);

    /**
     * 获取所有扩展插件列表
     */
    List<ISmExtensionPlugin> getAllExtensionPlugins();

    /**
     * 按扩展点查找插件
     */
    List<ISmExtensionPlugin> findPluginsBySlot(String slotId);

    /**
     * 按模块查找插件
     */
    List<ISmExtensionPlugin> findPluginsByModule(String moduleId);
}
