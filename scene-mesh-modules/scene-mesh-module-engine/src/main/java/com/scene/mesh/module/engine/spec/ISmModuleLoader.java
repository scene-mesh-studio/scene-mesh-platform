package com.scene.mesh.module.engine.spec;

/**
 * Module 加载器
 */
public interface ISmModuleLoader {

    /**
     * 获取 ModuleId
     */
    String getModuleId();
    /**
     * 加载 module
     */
    ISmModule loadModule() throws ModuleLoadException;

    class ModuleLoadException extends Exception {
        public ModuleLoadException(String message) {
            super(message);
        }

        public ModuleLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
