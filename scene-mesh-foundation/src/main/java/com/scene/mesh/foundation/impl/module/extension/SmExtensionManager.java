package com.scene.mesh.foundation.impl.module.extension;

import com.scene.mesh.foundation.spec.module.extension.ISmExtensionManager;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionPlugin;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionSlot;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 扩展管理器实现
 * 
 * 负责管理扩展槽和扩展插件的注册、注销、查找等功能
 * 提供线程安全的扩展管理能力
 */
@Slf4j
public class SmExtensionManager implements ISmExtensionManager {

    // 扩展槽存储 - slotId -> ISmExtensionSlot
    private final Map<String, ISmExtensionSlot> extensionSlots = new ConcurrentHashMap<>();
    
    // 扩展插件存储 - pluginId -> ISmExtensionPlugin
    private final Map<String, ISmExtensionPlugin> extensionPlugins = new ConcurrentHashMap<>();
    
    // 按扩展槽索引插件 - slotId -> List<ISmExtensionPlugin>
    private final Map<String, List<ISmExtensionPlugin>> pluginsBySlot = new ConcurrentHashMap<>();
    
    // 按模块索引插件 - moduleId -> List<ISmExtensionPlugin>
    private final Map<String, List<ISmExtensionPlugin>> pluginsByModule = new ConcurrentHashMap<>();
    
    // 读写锁，用于保护索引的更新操作
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public boolean registerExtensionSlot(ISmExtensionSlot extensionSlot) {
        if (extensionSlot == null) {
            log.warn("Cannot register null extension slot");
            return false;
        }
        
        String slotId = extensionSlot.getId();
        if (slotId == null || slotId.trim().isEmpty()) {
            log.warn("Cannot register extension slot with null or empty ID");
            return false;
        }
        
        try {
            ISmExtensionSlot existing = extensionSlots.putIfAbsent(slotId, extensionSlot);
            if (existing != null) {
                log.warn("Extension slot with ID '{}' already exists", slotId);
                return false;
            }
            
            // 初始化该槽的插件列表
            pluginsBySlot.put(slotId, new ArrayList<>());
            
            log.info("Successfully registered extension slot: {} with interface: {}", 
                    slotId, extensionSlot.getSlotInterfaceClass().getSimpleName());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to register extension slot: {}", slotId, e);
            return false;
        }
    }

    @Override
    public boolean unregisterExtensionSlot(String slotId) {
        if (slotId == null || slotId.trim().isEmpty()) {
            log.warn("Cannot unregister extension slot with null or empty ID");
            return false;
        }
        
        try {
            lock.writeLock().lock();
            try {
                ISmExtensionSlot removed = extensionSlots.remove(slotId);
                if (removed == null) {
                    log.warn("Extension slot with ID '{}' not found", slotId);
                    return false;
                }
                
                // 移除该槽的所有插件
                List<ISmExtensionPlugin> plugins = pluginsBySlot.remove(slotId);
                if (plugins != null) {
                    // 从插件存储中移除这些插件
                    for (ISmExtensionPlugin plugin : plugins) {
                        extensionPlugins.remove(plugin.getId());
                    }
                    
                    // 从模块索引中移除这些插件
                    for (ISmExtensionPlugin plugin : plugins) {
                        String moduleId = plugin.getModuleId();
                        List<ISmExtensionPlugin> modulePlugins = pluginsByModule.get(moduleId);
                        if (modulePlugins != null) {
                            modulePlugins.remove(plugin);
                            if (modulePlugins.isEmpty()) {
                                pluginsByModule.remove(moduleId);
                            }
                        }
                    }
                }
                
                log.info("Successfully unregistered extension slot: {}", slotId);
                return true;
                
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            log.error("Failed to unregister extension slot: {}", slotId, e);
            return false;
        }
    }

    @Override
    public boolean registerExtensionPlugin(ISmExtensionPlugin extensionPlugin) {
        if (extensionPlugin == null) {
            log.warn("Cannot register null extension plugin");
            return false;
        }
        
        String pluginId = extensionPlugin.getId();
        if (pluginId == null || pluginId.trim().isEmpty()) {
            log.warn("Cannot register extension plugin with null or empty ID");
            return false;
        }
        
        String slotId = extensionPlugin.getExtensionSlotId();
        if (slotId == null || slotId.trim().isEmpty()) {
            log.warn("Cannot register extension plugin with null or empty slot ID");
            return false;
        }
        
        // 检查扩展槽是否存在
        if (!extensionSlots.containsKey(slotId)) {
            log.warn("Cannot register plugin '{}' to non-existent slot '{}'", pluginId, slotId);
            return false;
        }
        
        try {
            lock.writeLock().lock();
            try {
                // 检查插件是否已存在
                ISmExtensionPlugin existing = extensionPlugins.putIfAbsent(pluginId, extensionPlugin);
                if (existing != null) {
                    log.warn("Extension plugin with ID '{}' already exists", pluginId);
                    return false;
                }
                
                // 添加到槽索引
                List<ISmExtensionPlugin> slotPlugins = pluginsBySlot.computeIfAbsent(slotId, k -> new ArrayList<>());
                slotPlugins.add(extensionPlugin);
                
                // 添加到模块索引
                String moduleId = extensionPlugin.getModuleId();
                if (moduleId != null && !moduleId.trim().isEmpty()) {
                    List<ISmExtensionPlugin> modulePlugins = pluginsByModule.computeIfAbsent(moduleId, k -> new ArrayList<>());
                    modulePlugins.add(extensionPlugin);
                }
                
                log.info("Successfully registered extension plugin: {} to slot: {} from module: {}", 
                        pluginId, slotId, moduleId);
                return true;
                
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            log.error("Failed to register extension plugin: {}", pluginId, e);
            return false;
        }
    }

    @Override
    public boolean unregisterExtensionPlugin(String pluginId) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            log.warn("Cannot unregister extension plugin with null or empty ID");
            return false;
        }
        
        try {
            lock.writeLock().lock();
            try {
                ISmExtensionPlugin plugin = extensionPlugins.remove(pluginId);
                if (plugin == null) {
                    log.warn("Extension plugin with ID '{}' not found", pluginId);
                    return false;
                }
                
                // 从槽索引中移除
                String slotId = plugin.getExtensionSlotId();
                List<ISmExtensionPlugin> slotPlugins = pluginsBySlot.get(slotId);
                if (slotPlugins != null) {
                    slotPlugins.remove(plugin);
                    if (slotPlugins.isEmpty()) {
                        pluginsBySlot.remove(slotId);
                    }
                }
                
                // 从模块索引中移除
                String moduleId = plugin.getModuleId();
                if (moduleId != null) {
                    List<ISmExtensionPlugin> modulePlugins = pluginsByModule.get(moduleId);
                    if (modulePlugins != null) {
                        modulePlugins.remove(plugin);
                        if (modulePlugins.isEmpty()) {
                            pluginsByModule.remove(moduleId);
                        }
                    }
                }
                
                log.info("Successfully unregistered extension plugin: {}", pluginId);
                return true;
                
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            log.error("Failed to unregister extension plugin: {}", pluginId, e);
            return false;
        }
    }

    @Override
    public ISmExtensionSlot findExtensionSlot(String slotId) {
        if (slotId == null || slotId.trim().isEmpty()) {
            return null;
        }
        return extensionSlots.get(slotId);
    }

    @Override
    public List<ISmExtensionSlot> getAllExtensionSlots() {
        return new ArrayList<>(extensionSlots.values());
    }

    @Override
    public ISmExtensionPlugin findExtensionPlugin(String pluginId) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return null;
        }
        return extensionPlugins.get(pluginId);
    }

    @Override
    public List<ISmExtensionPlugin> getAllExtensionPlugins() {
        return new ArrayList<>(extensionPlugins.values());
    }

    @Override
    public List<ISmExtensionPlugin> findPluginsBySlot(String slotId) {
        if (slotId == null || slotId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        lock.readLock().lock();
        try {
            List<ISmExtensionPlugin> plugins = pluginsBySlot.get(slotId);
            return plugins != null ? new ArrayList<>(plugins) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ISmExtensionPlugin> findPluginsByModule(String moduleId) {
        if (moduleId == null || moduleId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        lock.readLock().lock();
        try {
            List<ISmExtensionPlugin> plugins = pluginsByModule.get(moduleId);
            return plugins != null ? new ArrayList<>(plugins) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取扩展管理器的统计信息
     */
    public Map<String, Object> getStatistics() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSlots", extensionSlots.size());
            stats.put("totalPlugins", extensionPlugins.size());
            stats.put("slotsWithPlugins", pluginsBySlot.size());
            stats.put("modulesWithPlugins", pluginsByModule.size());
            
            // 按槽统计插件数量
            Map<String, Integer> pluginsPerSlot = pluginsBySlot.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().size()
                    ));
            stats.put("pluginsPerSlot", pluginsPerSlot);
            
            // 按模块统计插件数量
            Map<String, Integer> pluginsPerModule = pluginsByModule.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().size()
                    ));
            stats.put("pluginsPerModule", pluginsPerModule);
            
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 清空所有扩展槽和插件
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            extensionSlots.clear();
            extensionPlugins.clear();
            pluginsBySlot.clear();
            pluginsByModule.clear();
            log.info("Cleared all extension slots and plugins");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
