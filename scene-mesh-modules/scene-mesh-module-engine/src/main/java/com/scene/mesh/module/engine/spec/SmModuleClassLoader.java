package com.scene.mesh.module.engine.spec;

import lombok.Getter;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SmModuleClassLoader extends URLClassLoader {

    // 平台共享类 - 由平台提供
    private static final Set<String> PLATFORM_SHARED_CLASSES_PREFIX = Set.of(
            // 模块API接口
            "com.scene.mesh.module.api."
    );

    // 系统类 - 由 JVM 提供
    private static final Set<String> SYSTEM_CLASSES_PREFIX = Set.of(
            "java.", "javax.", "jdk.", "com.sun.", "sun.",
            "org.ietf.", "org.omg.", "org.w3c.", "org.xml."
    );

    @Getter
    private final String moduleId;
    @Getter
    private final String jarPath;
    private final ClassLoader platformClassLoader;
    private final ConcurrentHashMap<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    public SmModuleClassLoader(String moduleId, URL moduleUrl, ClassLoader platformClassLoader) {
        super(new URL[]{moduleUrl}, null);
        this.moduleId = moduleId;
        this.jarPath = moduleUrl.getPath();
        this.platformClassLoader = platformClassLoader;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. 检查是否已经加载过
        Class<?> clazz = loadedClasses.get(name);
        if (clazz != null) {
            return clazz;
        }

        // 2. 系统类 - 使用系统类加载器
        if (isSystemClass(name)) {
            clazz = getSystemClassLoader().loadClass(name);
            loadedClasses.put(name, clazz);
            return clazz;
        }

        // 3. 平台共享类 - 使用平台类加载器
        if (isPlatformSharedClass(name)) {
            clazz = platformClassLoader.loadClass(name);
            loadedClasses.put(name, clazz);
            return clazz;
        }

        // 4. 模块私有类 - 使用当前加载器
        try {
            clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            loadedClasses.put(name, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            // 5. 如果找不到，抛出异常，不进行回退
            throw new ClassNotFoundException("Class not found in module " + moduleId + ": " + name, e);
        }
    }

    private boolean isSystemClass(String name) {
        return SYSTEM_CLASSES_PREFIX.stream().anyMatch(name::startsWith);
    }

    private boolean isPlatformSharedClass(String name) {
        return PLATFORM_SHARED_CLASSES_PREFIX.stream().anyMatch(name::startsWith);
    }

}
