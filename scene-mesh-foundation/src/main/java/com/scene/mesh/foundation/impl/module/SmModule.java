package com.scene.mesh.foundation.impl.module;

import com.scene.mesh.foundation.spec.module.ISmModule;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionPlugin;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SmModule implements ISmModule {

    private String id;
    private String name;
    private String modulePath;
    private String version;
    private List<ISmExtensionPlugin> extensionCapacities;
    private Map<String, Object> metas;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getPath() {
        return this.modulePath;
    }

    @Override
    public List<ISmExtensionPlugin> getExtensionCapacities() {
        return this.extensionCapacities;
    }

    @Override
    public Map<String, Object> getMetas() {
        return this.metas;
    }
}
