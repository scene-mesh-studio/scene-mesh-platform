package com.scene.mesh.foundation.spec.module;

/**
 * Module 加载器
 */
public interface ISmModuleLoader {

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
