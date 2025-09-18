package com.scene.mesh.module.engine.impl.extension;

import com.scene.mesh.module.engine.spec.extension.ISmExtensionPlugin;

import java.util.Objects;

/**
 * 扩展插件实现
 */
public class SmExtensionPlugin implements ISmExtensionPlugin {
    
    private final String id;
    private final String name;
    private final String extensionSlotId;
    private final String moduleId;
    private final Object pluginInstance;
    private final Class<?> pluginInterfaceClass;
    
    public SmExtensionPlugin(String id, String name, String extensionSlotId, 
                           String moduleId, Object pluginInstance, Class<?> pluginInterfaceClass) {
        this.id = id;
        this.name = name;
        this.extensionSlotId = extensionSlotId;
        this.moduleId = moduleId;
        this.pluginInstance = pluginInstance;
        this.pluginInterfaceClass = pluginInterfaceClass;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getExtensionSlotId() {
        return extensionSlotId;
    }
    
    @Override
    public String getModuleId() {
        return moduleId;
    }
    
    @Override
    public Object getPluginInstance() {
        return pluginInstance;
    }
    
    @Override
    public Class<?> getPluginInterfaceClass() {
        return pluginInterfaceClass;
    }
    
    @Override
    public String toString() {
        return "SmExtensionPlugin{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", extensionSlotId='" + extensionSlotId + '\'' +
                ", moduleId='" + moduleId + '\'' +
                ", pluginInterfaceClass=" + pluginInterfaceClass +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SmExtensionPlugin that = (SmExtensionPlugin) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
