package com.scene.mesh.foundation.spec.module;

public interface ISmModuleManager {

    /**
     * 加载 module
     * @param modulePath module 路径，当前支持 URL
     */
    ISmModule loadModule(String moduleId, String modulePath) throws ISmModuleLoader.ModuleLoadException;

    void unloadModule(String moduleId) throws ISmModuleLoader.ModuleLoadException;

    /**
     * 获取 module
     */
    ISmModule getModule(String moduleId);

    boolean isModuleLoaded(String moduleId);

    void reloadModule(String moduleId) throws ISmModuleLoader.ModuleLoadException;
}
