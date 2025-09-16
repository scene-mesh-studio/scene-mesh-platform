package com.scene.mesh.foundation.spec.module.extension;

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
     * 按指定plugin调用
     */
    Object invokeExtension(String pluginId, String methodName, Object... args);
}
