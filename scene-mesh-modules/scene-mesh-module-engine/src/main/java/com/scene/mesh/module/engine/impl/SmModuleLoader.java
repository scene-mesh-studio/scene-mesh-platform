package com.scene.mesh.module.engine.impl;

import com.scene.mesh.module.engine.impl.scanner.ExtensionPluginScanner;
import com.scene.mesh.module.engine.spec.ISmModule;
import com.scene.mesh.module.engine.spec.ISmModuleLoader;
import com.scene.mesh.module.engine.spec.SmModuleClassLoader;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionPlugin;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Slf4j
public class SmModuleLoader implements ISmModuleLoader {

    private final String moduleId;
    private final ClassLoader classLoader;
    private final ExtensionPluginScanner extensionPluginScanner;

    public SmModuleLoader(String moduleId, ClassLoader classLoader) {
        this.moduleId = moduleId;
        this.classLoader = classLoader;
        this.extensionPluginScanner = new ExtensionPluginScanner();
    }

    @Override
    public String getModuleId() {
        return moduleId;
    }

    @Override
    public ISmModule loadModule() throws ModuleLoadException {
        try {
            // 从 SmModuleClassLoader 中获取 JAR 文件路径
            if (classLoader instanceof SmModuleClassLoader) {
                SmModuleClassLoader moduleClassLoader = (SmModuleClassLoader) classLoader;
                String jarPath = moduleClassLoader.getJarPath();
                
                // 解析模块路径为 File 对象
                File jarFile = parseModulePathToFile(jarPath);
                
                // 从 JAR 文件创建模块对象
                SmModule module = createModuleFromJar(jarFile, jarPath);
                
                log.info("Successfully loaded module: {} from path: {}", module.getId(), jarPath);
                return module;
            } else {
                throw new ModuleLoadException("ClassLoader is not a SmModuleClassLoader");
            }

        } catch (IOException e) {
            throw new ModuleLoadException("Failed to load module", e);
        } catch (Exception e) {
            throw new ModuleLoadException("Unexpected error loading module", e);
        }
    }


    /**
     * 解析模块路径为 File 对象
     */
    private File parseModulePathToFile(String modulePath) throws ModuleLoadException {
        String trimmedPath = modulePath.trim();
        
        if (trimmedPath.startsWith("http://") || trimmedPath.startsWith("https://")) {
            // TODO
            // 对于远程 URL，我们需要先下载到本地临时文件
            // 这里简化处理，实际项目中可能需要实现下载逻辑
            throw new ModuleLoadException("Remote JAR loading not implemented yet: " + trimmedPath);
        } else {
            // 本地 JAR 文件
            File file = new File(trimmedPath);
            
            if (!file.exists()) {
                throw new ModuleLoadException("JAR file does not exist: " + trimmedPath);
            }
            
            if (!file.getName().toLowerCase().endsWith(".jar")) {
                throw new ModuleLoadException("Only JAR files are supported: " + trimmedPath);
            }
            
            return file;
        }
    }

    /**
     * 从 JAR 文件创建模块对象
     */
    private SmModule createModuleFromJar(File jarFile, String modulePath) throws IOException {
        try {
            JarFile jar = new JarFile(jarFile);
            // 读取 Manifest
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                // 从 MANIFEST.MF 创建模块对象
                SmModule module = createModuleFromManifest(manifest, jarFile, modulePath);
                
                // 扫描并设置扩展插件
                List<ISmExtensionPlugin> plugins = extensionPluginScanner.findExtensionPlugins(
                        jarFile.getAbsolutePath(), module.getId(), classLoader);
                module.setExtensionCapacities(plugins);

                return module;
            }

            throw new IOException("Failed to load module - cannot find manifest file in jar: " + jarFile.getAbsolutePath());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从 MANIFEST.MF 创建模块对象
     */
    private SmModule createModuleFromManifest(Manifest manifest, File jarFile, String modulePath) {
        SmModule module = new SmModule();
        
        // 从 MANIFEST.MF 读取模块信息
        String moduleId = manifest.getMainAttributes().getValue("Module-Id");
        String moduleName = manifest.getMainAttributes().getValue("Module-Name");
        String moduleVersion = manifest.getMainAttributes().getValue("Module-Version");
        String moduleDescription = manifest.getMainAttributes().getValue("Module-Description");
        String moduleAuthor = manifest.getMainAttributes().getValue("Module-Author");
        
        // 设置模块基本信息
        module.setId(moduleId != null ? moduleId : extractModuleIdFromJar(jarFile));
        module.setName(moduleName != null ? moduleName : extractModuleNameFromJar(jarFile));
        module.setVersion(moduleVersion != null ? moduleVersion : "1.0.0");
        module.setModulePath(modulePath);
        
        // 创建元数据
        java.util.Map<String, Object> metas = new java.util.HashMap<>();
        metas.put("jarFile", jarFile.getAbsolutePath());
        metas.put("jarSize", jarFile.length());
        metas.put("loadTime", System.currentTimeMillis());
        metas.put("hasManifest", true);
        metas.put("mainClass", manifest.getMainAttributes().getValue("Main-Class"));
        metas.put("moduleDescription", moduleDescription);
        metas.put("moduleAuthor", moduleAuthor);
        
        // 添加其他 MANIFEST 属性
        for (java.util.Map.Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("Module-") || key.startsWith("Scene-Mesh-")) {
                metas.put(key, entry.getValue());
            }
        }
        
        module.setMetas(metas);
        
        return module;
    }

    /**
     * 从 JAR 文件名提取模块 ID
     */
    private String extractModuleIdFromJar(File jarFile) {
        String fileName = jarFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        // 移除版本号模式 (例如: module-name-1.0.0.jar -> module-name)
        if (baseName.matches(".*-\\d+\\.\\d+\\.\\d+.*")) {
            baseName = baseName.replaceAll("-\\d+\\.\\d+\\.\\d+.*", "");
        }
        
        // 转换为模块 ID 格式
        return baseName.toLowerCase().replaceAll("-", "_");
    }

    /**
     * 从 JAR 文件名提取模块名称
     */
    private String extractModuleNameFromJar(File jarFile) {
        String fileName = jarFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        // 移除版本号模式 (例如: module-name-1.0.0.jar -> module-name)
        if (baseName.matches(".*-\\d+\\.\\d+\\.\\d+.*")) {
            baseName = baseName.replaceAll("-\\d+\\.\\d+\\.\\d+.*", "");
        }
        
        // 转换为更友好的名称
        return baseName.replaceAll("-", " ").replaceAll("_", " ");
    }
}
