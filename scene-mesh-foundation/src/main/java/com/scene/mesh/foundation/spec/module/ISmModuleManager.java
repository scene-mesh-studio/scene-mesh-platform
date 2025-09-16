package com.scene.mesh.foundation.spec.module;

public interface ISmModuleManager {

    /**
     * 加载 module
     * @param modulePath module 路径，当前支持 URL
     */
    ISmModule loadModule(String modulePath);

    /**
     * 注册 module
     */
    boolean registerModule(ISmModule module);

    /**
     * 注销 module
     */
    void unregisterModule(String moduleId);

    /**
     * 获取 module
     */
    ISmModule getModule(String moduleId);
}
