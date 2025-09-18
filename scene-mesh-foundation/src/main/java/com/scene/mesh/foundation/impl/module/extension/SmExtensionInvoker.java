package com.scene.mesh.foundation.impl.module.extension;

import com.scene.mesh.foundation.spec.module.extension.ISmExtensionInvoker;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionManager;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionPlugin;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionSlot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展调用器实现
 * 负责调用扩展插件的方法
 */
public class SmExtensionInvoker implements ISmExtensionInvoker {
    
    private final ISmExtensionManager extensionManager;
    
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
                System.err.println("Failed to invoke extension " + plugin.getId() + 
                    " method " + methodName + ": " + e.getMessage());
            }
        }
        
        return results;
    }
    
    @Override
    public Object invokeExtension(String pluginId, String methodName, Object... args) {
        ISmExtensionPlugin plugin = extensionManager.findExtensionPlugin(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Extension plugin not found: " + pluginId);
        }
        
        Object instance = plugin.getPluginInstance();
        if (instance == null) {
            throw new IllegalStateException("Extension plugin instance is null: " + pluginId);
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
                    " with parameter types: " + java.util.Arrays.toString(paramTypes));
            }
            
            // 调用方法
            method.setAccessible(true);
            return method.invoke(instance, args);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke extension " + pluginId + 
                " method " + methodName, e);
        }
    }
    
    /**
     * 查找方法，支持参数类型匹配
     */
    private Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            // 首先尝试精确匹配
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // 如果精确匹配失败，尝试查找兼容的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && 
                    isCompatible(method.getParameterTypes(), paramTypes)) {
                    return method;
                }
            }
            return null;
        }
    }
    
    /**
     * 检查参数类型是否兼容
     */
    private boolean isCompatible(Class<?>[] methodParams, Class<?>[] callParams) {
        if (methodParams.length != callParams.length) {
            return false;
        }
        
        for (int i = 0; i < methodParams.length; i++) {
            if (callParams[i] == null) {
                // null 参数可以赋值给任何非基本类型
                if (methodParams[i].isPrimitive()) {
                    return false;
                }
            } else if (!methodParams[i].isAssignableFrom(callParams[i])) {
                return false;
            }
        }
        
        return true;
    }
}
