package com.scene.mesh.module.engine.impl.scanner;

import com.scene.mesh.module.api.annotation.ExtensionPlugin;
import com.scene.mesh.module.engine.impl.extension.SmExtensionPlugin;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ExtensionPlugin 注解扫描器
 */
@Slf4j
public class ExtensionPluginScanner {

    public List<ISmExtensionPlugin> findExtensionPlugins(String jarPath, String moduleId, ClassLoader classLoader) {
        List<ISmExtensionPlugin> plugins = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 只处理.class文件
                if (entryName.endsWith(".class") && !entryName.contains("$")) {
                    try {
                        ClassReader classReader = new ClassReader(jarFile.getInputStream(entry));
                        ExtensionPluginAnnotationVisitor visitor = new ExtensionPluginAnnotationVisitor();
                        classReader.accept(visitor, ClassReader.SKIP_CODE);

                        // 如果找到了ExtensionPlugin注解
                        if (visitor.hasExtensionPluginAnnotation()) {
                            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);

                            try {
                                // 使用类加载器加载类
                                Class<?> clazz = classLoader.loadClass(className);

                                // 创建扩展插件包装器
                                ISmExtensionPlugin plugin = createExtensionPlugin(clazz, moduleId);
                                if (plugin != null) {
                                    plugins.add(plugin);
                                    log.info("Found extension plugin: {} in module: {}", className, moduleId);
                                }
                            } catch (Exception e) {
                                log.error("Failed to load extension plugin class: {} in module: {}", className, moduleId, e);
                            }
                        }
                    } catch (IOException e) {
                        log.warn("Failed to read class file: {} in module: {}", entryName, moduleId, e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan JAR file: {} for module: {}", jarPath, moduleId, e);
        }

        log.info("Found {} extension plugins in module: {}", plugins.size(), moduleId);
        return plugins;
    }


    /**
     * 创建扩展插件包装器
     */
    private ISmExtensionPlugin createExtensionPlugin(Class<?> clazz, String moduleId) {
        try {
            // 获取@ExtensionPlugin注解
            ExtensionPlugin annotation = clazz.getAnnotation(ExtensionPlugin.class);
            if (annotation == null) {
                return null;
            }

            // 实例化扩展实现类
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 获取实现的接口
            Class<?>[] interfaces = getAllInterfaces(clazz);
            if (interfaces.length == 0) {
                log.warn("Extension plugin class {} does not implement any interface", clazz.getName());
                return null;
            }

            // 包装
            return new SmExtensionPlugin(
                    annotation.pluginId(),
                    annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name(),
                    annotation.slotId(),
                    moduleId,
                    instance,
                    interfaces[0]// 使用第一个接口
            );

        } catch (Exception e) {
            log.error("Failed to create extension plugin for class: {}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 获取类及其所有父类实现的接口
     */
    private Class<?>[] getAllInterfaces(Class<?> clazz) {
        java.util.Set<Class<?>> interfaceSet = new java.util.HashSet<>();
        
        // 遍历整个继承链
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            // 添加当前类直接实现的接口
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                interfaceSet.add(interfaceClass);
                // 递归添加接口的父接口
                addSuperInterfaces(interfaceClass, interfaceSet);
            }
            currentClass = currentClass.getSuperclass();
        }
        
        return interfaceSet.toArray(new Class<?>[0]);
    }
    
    /**
     * 递归添加接口的父接口
     */
    private void addSuperInterfaces(Class<?> interfaceClass, java.util.Set<Class<?>> interfaceSet) {
        Class<?>[] superInterfaces = interfaceClass.getInterfaces();
        for (Class<?> superInterface : superInterfaces) {
            if (interfaceSet.add(superInterface)) {
                addSuperInterfaces(superInterface, interfaceSet);
            }
        }
    }

    /**
     * Spring ASM类访问器，用于扫描@ExtensionPlugin注解
     */
    private static class ExtensionPluginAnnotationVisitor extends ClassVisitor {
        private boolean hasExtensionPlugin = false;

        public ExtensionPluginAnnotationVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // 检查是否是@ExtensionPlugin注解
            if ("Lcom/scene/mesh/module/api/annotation/ExtensionPlugin;".equals(descriptor)) {
                hasExtensionPlugin = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        public boolean hasExtensionPluginAnnotation() {
            return hasExtensionPlugin;
        }
    }
}
