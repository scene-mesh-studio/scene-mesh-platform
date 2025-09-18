package com.scene.mesh.module.engine.spec.extension;

import java.util.List;

/**
 * 扩展调用器
 */
public interface ISmExtensionInvoker {
    /**
     * 获取扩展实例列表
     */
    List<Object> getExtensionInstances(String slotId);

    /**
     * 按slot调用所有plugin
     */
    List<Object> invokeAllExtensions(String slotId, String methodName, Object... args);

    /**
     * 按 slot 调用指定 plugin
     */
    Object invokeExtension(String slotId, String pluginId, String methodName, Object... args);

    /**
     * 按指定plugin调用
     */
    Object invokeExtension(String pluginId, String methodName, Object... args);
}
