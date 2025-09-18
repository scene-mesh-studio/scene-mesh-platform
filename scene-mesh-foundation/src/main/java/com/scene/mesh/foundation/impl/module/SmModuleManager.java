package com.scene.mesh.foundation.impl.module;

import com.scene.mesh.foundation.spec.module.ISmModule;
import com.scene.mesh.foundation.spec.module.ISmModuleLoader;
import com.scene.mesh.foundation.spec.module.ISmModuleManager;
import com.scene.mesh.foundation.spec.module.SmModuleClassLoader;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionManager;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionPlugin;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SmModuleManager implements ISmModuleManager {

    private final Map<String, ISmModule> modules = new ConcurrentHashMap<>();
    private final Map<String, SmModuleClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final ISmExtensionManager extensionManager;

    public SmModuleManager(ISmExtensionManager extensionManager) {
        this.extensionManager = extensionManager;
    }

    @Override
    public ISmModule loadModule(String moduleId, String modulePath) throws ISmModuleLoader.ModuleLoadException {
        try {
            // 检查模块是否已经加载
            if (modules.containsKey(moduleId)) {
                log.warn("Module {} is already loaded", moduleId);
                return modules.get(moduleId);
            }

            // 创建模块类加载器
            SmModuleClassLoader classLoader = createModuleClassLoader(moduleId, modulePath);
            classLoaders.put(moduleId, classLoader);

            // 创建模块加载器
            ISmModuleLoader moduleLoader = new SmModuleLoader(moduleId, classLoader);

            // 加载模块
            ISmModule module = moduleLoader.loadModule();
            modules.put(moduleId, module);

            // 注册扩展插件
            registerExtensionPlugins(module);

            log.info("Successfully loaded module: {} from path: {}", moduleId, modulePath);
            return module;

        } catch (Exception e) {
            log.error("Failed to load module: {} from path: {}", moduleId, modulePath, e);
            throw new ISmModuleLoader.ModuleLoadException("Failed to load module: " + moduleId, e);
        }
    }

    @Override
    public void unloadModule(String moduleId) throws ISmModuleLoader.ModuleLoadException {
        try {
            // 检查模块是否存在
            ISmModule module = modules.get(moduleId);
            if (module == null) {
                log.warn("Module {} is not loaded", moduleId);
                return;
            }

            // 注销扩展插件
            unregisterExtensionPlugins(module);

            // 移除模块
            modules.remove(moduleId);

            // 关闭类加载器
            SmModuleClassLoader classLoader = classLoaders.remove(moduleId);
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (Exception e) {
                    log.warn("Failed to close class loader for module: {}", moduleId, e);
                }
            }

            log.info("Successfully unloaded module: {}", moduleId);

        } catch (Exception e) {
            log.error("Failed to unload module: {}", moduleId, e);
            throw new ISmModuleLoader.ModuleLoadException("Failed to unload module: " + moduleId, e);
        }
    }

    @Override
    public ISmModule getModule(String moduleId) {
        return modules.get(moduleId);
    }

    @Override
    public boolean isModuleLoaded(String moduleId) {
        return modules.containsKey(moduleId);
    }

    @Override
    public void reloadModule(String moduleId) throws ISmModuleLoader.ModuleLoadException {
        try {
            // 获取模块路径
            ISmModule module = modules.get(moduleId);
            if (module == null) {
                throw new ISmModuleLoader.ModuleLoadException("Module " + moduleId + " is not loaded");
            }

            String modulePath = module.getPath();
            if (modulePath == null) {
                throw new ISmModuleLoader.ModuleLoadException("Module " + moduleId + " has no module path");
            }

            // 先卸载
            unloadModule(moduleId);

            // 再加载
            loadModule(moduleId, modulePath);

            log.info("Successfully reloaded module: {}", moduleId);

        } catch (Exception e) {
            log.error("Failed to reload module: {}", moduleId, e);
            throw new ISmModuleLoader.ModuleLoadException("Failed to reload module: " + moduleId, e);
        }
    }

    /**
     * 创建模块类加载器
     */
    private SmModuleClassLoader createModuleClassLoader(String moduleId, String modulePath) throws ISmModuleLoader.ModuleLoadException {
        try {
            // 解析模块路径
            File moduleFile = new File(modulePath);
            if (!moduleFile.exists()) {
                throw new ISmModuleLoader.ModuleLoadException("Module file does not exist: " + modulePath);
            }

            // 创建URL
            URL moduleUrl = moduleFile.toURI().toURL();

            // 创建类加载器
            ClassLoader platformClassLoader = getClass().getClassLoader();
            return new SmModuleClassLoader(moduleId, moduleUrl, platformClassLoader);

        } catch (MalformedURLException e) {
            throw new ISmModuleLoader.ModuleLoadException("Invalid module path: " + modulePath, e);
        }
    }

    /**
     * 注册扩展插件
     */
    private void registerExtensionPlugins(ISmModule module) {
        List<ISmExtensionPlugin> plugins = module.getExtensionCapacities();
        if (plugins != null) {
            for (ISmExtensionPlugin plugin : plugins) {
                try {
                    extensionManager.registerExtensionPlugin(plugin);
                    log.info("Registered extension plugin: {} for module: {}",
                            plugin.getId(), module.getId());
                } catch (Exception e) {
                    log.error("Failed to register extension plugin: {} for module: {}",
                            plugin.getId(), module.getId(), e);
                }
            }
        }
    }

    /**
     * 注销扩展插件
     */
    private void unregisterExtensionPlugins(ISmModule module) {
        List<ISmExtensionPlugin> plugins = module.getExtensionCapacities();
        if (plugins != null) {
            for (ISmExtensionPlugin plugin : plugins) {
                try {
                    extensionManager.unregisterExtensionPlugin(plugin.getId());
                    log.info("Unregistered extension plugin: {} for module: {}",
                            plugin.getId(), module.getId());
                } catch (Exception e) {
                    log.error("Failed to unregister extension plugin: {} for module: {}",
                            plugin.getId(), module.getId(), e);
                }
            }
        }
    }
}