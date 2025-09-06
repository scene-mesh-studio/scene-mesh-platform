package com.scene.mesh.foundation.impl.component;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Spring 应用上下文和资源加载工具
 * 统一管理所有 Spring 相关的资源加载和类加载器问题
 */
public class SpringApplicationContextUtils {

    @lombok.Setter
    private static ApplicationContext applicationContext;
    @lombok.Setter
    @lombok.Getter
    private static String contextId;
    @lombok.Setter
    private static Class<?>[] contextClass;

    // ========== 原有的 ApplicationContext 管理方法 ==========

    public static synchronized ApplicationContext getApplicationContext() {
        if (applicationContext != null) {
            return applicationContext;
        } else {
            try {
                String resourcePath = contextId == null ? "work.xml" : contextId;

                // 使用统一的资源查找方法
                ClassPathResource resource = findClassPathResource(resourcePath);

                // 使用找到的类加载器来创建 Spring 上下文
                System.out.println("Creating Spring context with classloader: " +
                        resource.getClassLoader().getClass().getName() + "@" +
                        Integer.toHexString(resource.getClassLoader().hashCode()));

                ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
                context.setClassLoader(resource.getClassLoader());
                context.setConfigLocation(resourcePath);
                context.refresh();

                applicationContext = context;
                return applicationContext;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load Spring context from: " +
                        (contextId == null ? "work.xml" : contextId), e);
            }
        }
    }

    public static synchronized ApplicationContext getApplicationContextByAnnotation() {
        if (applicationContext != null) {
            return applicationContext;
        } else {
            applicationContext = new AnnotationConfigApplicationContext(contextClass);
            return applicationContext;
        }
    }

    // ========== 新增的统一资源管理方法 ==========

    /**
     * 统一的 ClassPathResource 查找方法
     * 遍历整个类加载器链来查找资源
     *
     * @param resourcePath 资源路径
     * @return 找到资源的 ClassPathResource，如果没找到抛出异常
     */
    public static ClassPathResource findClassPathResource(String resourcePath) {
        ClassLoader foundClassLoader = findResourceInClassLoaderChain(resourcePath);
        if (foundClassLoader == null) {
            throw new RuntimeException("Cannot find resource: " + resourcePath +
                    " in any classloader in the chain.");
        }
        return new ClassPathResource(resourcePath, foundClassLoader);
    }

    /**
     * 统一的 YAML 配置文件加载方法
     *
     * @param yamlPath YAML 文件路径
     * @return 加载的 Properties 对象
     */
    public static Properties loadYamlProperties(String yamlPath) {
        ClassPathResource resource = findClassPathResource(yamlPath);
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resource);
        return Objects.requireNonNull(yaml.getObject());
    }

    /**
     * 检查资源是否存在于类路径中
     *
     * @param resourcePath 资源路径
     * @return 如果资源存在返回 true，否则返回 false
     */
    public static boolean resourceExists(String resourcePath) {
        return findResourceInClassLoaderChain(resourcePath) != null;
    }

    /**
     * 遍历整个类加载器链来查找资源
     * @param resourcePath 资源路径
     * @return 找到资源的类加载器，如果没找到返回null
     */
    private static ClassLoader findResourceInClassLoaderChain(String resourcePath) {
        List<ClassLoader> classLoaders = new ArrayList<>();

        // 1. 当前线程的上下文类加载器
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            classLoaders.add(contextClassLoader);
        }

        // 2. 当前类的类加载器
        ClassLoader currentClassLoader = SpringApplicationContextUtils.class.getClassLoader();
        if (currentClassLoader != null) {
            classLoaders.add(currentClassLoader);
        }

        // 3. 系统类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if (systemClassLoader != null) {
            classLoaders.add(systemClassLoader);
        }

        // 4. 遍历类加载器链
        for (ClassLoader classLoader : classLoaders) {
            ClassLoader current = classLoader;
            while (current != null) {
                try {
                    InputStream inputStream = current.getResourceAsStream(resourcePath);
                    if (inputStream != null) {
                        inputStream.close();
                        System.out.println("Found resource '" + resourcePath + "' in classloader: " +
                                current.getClass().getName() + "@" + Integer.toHexString(current.hashCode()));
                        return current;
                    }
                } catch (Exception e) {
                    // 忽略异常，继续尝试下一个类加载器
                }

                // 获取父类加载器
                current = current.getParent();
            }
        }

        // 5. 如果还没找到，输出调试信息
        System.out.println("Resource '" + resourcePath + "' not found in classloader chain:");
        for (ClassLoader classLoader : classLoaders) {
            ClassLoader current = classLoader;
            int level = 0;
            while (current != null) {
                System.out.println("  Level " + level + ": " + current.getClass().getName() +
                        "@" + Integer.toHexString(current.hashCode()));
                current = current.getParent();
                level++;
            }
        }

        return null;
    }
}