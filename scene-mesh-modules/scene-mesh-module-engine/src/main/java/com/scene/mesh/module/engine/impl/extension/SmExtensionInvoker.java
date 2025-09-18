package com.scene.mesh.module.engine.impl.extension;

import com.scene.mesh.module.engine.spec.extension.ISmExtensionInvoker;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionManager;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionPlugin;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展调用器实现
 * 负责调用扩展插件的方法
 */
@Slf4j
public class SmExtensionInvoker implements ISmExtensionInvoker {

    private final ISmExtensionManager extensionManager;

    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    public SmExtensionInvoker(ISmExtensionManager extensionManager) {
        this.extensionManager = extensionManager;
    }

    @Override
    public List<Object> getExtensionInstances(String slotId) {
        List<Object> instances = new ArrayList<>();
        List<ISmExtensionPlugin> plugins = extensionManager.findPluginsBySlot(slotId);

        for (ISmExtensionPlugin plugin : plugins) {
            Object instance = plugin.getPluginInstance();
            if (instance != null) {
                instances.add(instance);
            }
        }

        return instances;
    }

    @Override
    public List<Object> invokeAllExtensions(String slotId, String methodName, Object... args) {
        List<Object> results = new ArrayList<>();
        List<ISmExtensionPlugin> plugins = extensionManager.findPluginsBySlot(slotId);

        for (ISmExtensionPlugin plugin : plugins) {
            try {
                Object result = invokeExtension(plugin.getId(), methodName, args);
                results.add(result);
            } catch (Exception e) {
                // 记录错误但不中断其他插件的调用
                log.error("Failed to invoke extension plugin - {}, method - {}, message - {}",
                        plugin.getId(), methodName, e.getMessage());
            }
        }

        return results;
    }

    @Override
    public Object invokeExtension(String slotId, String pluginId, String methodName, Object... args) {
        List<ISmExtensionPlugin> plugins = extensionManager.findPluginsBySlot(slotId);
        if (plugins == null || plugins.isEmpty()) {
            throw new IllegalArgumentException("Extension plugins not found from slotId: " + slotId);
        }

        ISmExtensionPlugin targetPlugin = null;
        for (ISmExtensionPlugin plugin : plugins) {
            if (plugin.getId().equals(pluginId)) {
                targetPlugin = plugin;
            }
        }

        if (targetPlugin == null) {
            throw new IllegalArgumentException("Extension plugin not found: " + pluginId);
        }

        return invokePlugin(methodName, args, targetPlugin);
    }

    @Override
    public Object invokeExtension(String pluginId, String methodName, Object... args) {
        ISmExtensionPlugin plugin = extensionManager.findExtensionPlugin(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Extension plugin not found: " + pluginId);
        }

        return invokePlugin(methodName, args, plugin);
    }

    private Object invokePlugin(String methodName, Object[] args, ISmExtensionPlugin plugin) {
        Object instance = plugin.getPluginInstance();
        if (instance == null) {
            throw new IllegalStateException("Extension plugin instance is null: " + plugin.getId());
        }

        try {
            // 获取方法参数类型
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }

            // 查找方法
            Method method = findMethod(instance.getClass(), methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName +
                        " with parameter types: " + Arrays.toString(paramTypes));
            }

            // 调用方法
            method.setAccessible(true);
            return method.invoke(instance, args);

        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke extension " + plugin.getId() +
                    " method " + methodName, e);
        }
    }

    /**
     * 查找方法，支持参数类型匹配和缓存
     */
    private Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        // 生成缓存key
        String cacheKey = generateCacheKey(clazz.getName(), methodName, paramTypes);

        // 先从缓存中查找
        Method cachedMethod = methodCache.get(cacheKey);
        if (cachedMethod != null) {
            return cachedMethod;
        }

        Method method = null;
        try {
            // 首先尝试直接匹配
            method = clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // 如果直接匹配失败，尝试接口类型匹配
            method = findMethodWithInterfaceMatching(clazz, methodName, paramTypes);
        }

        // 将找到的方法放入缓存
        if (method != null) {
            methodCache.put(cacheKey, method);
        }

        return method;
    }

    /**
     * 使用接口类型匹配查找方法
     */
    private Method findMethodWithInterfaceMatching(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            
            Class<?>[] methodParamTypes = method.getParameterTypes();
            if (methodParamTypes.length != paramTypes.length) {
                continue;
            }
            
            boolean match = true;
            for (int i = 0; i < paramTypes.length; i++) {
                if (!isAssignableFrom(methodParamTypes[i], paramTypes[i])) {
                    match = false;
                    break;
                }
            }
            
            if (match) {
                return method;
            }
        }
        
        return null;
    }

    /**
     * 检查类型是否可赋值
     */
    private boolean isAssignableFrom(Class<?> target, Class<?> source) {
        if (target.isAssignableFrom(source)) {
            return true;
        }
        
        // 特殊处理 Map 接口类型匹配
        if (target == Map.class && source != null) {
            return Map.class.isAssignableFrom(source);
        }
        
        return false;
    }

    /**
     * 生成缓存key
     */
    private String generateCacheKey(String className, String methodName, Class<?>[] paramTypes) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(className).append("#").append(methodName);

        if (paramTypes != null && paramTypes.length > 0) {
            keyBuilder.append("#");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) {
                    keyBuilder.append(",");
                }
                keyBuilder.append(paramTypes[i].getName());
            }
        }

        return keyBuilder.toString();
    }
}
