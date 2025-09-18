package com.scene.mesh.foundation.impl.module;

import java.util.List;
import java.util.Map;
import com.scene.mesh.foundation.spec.module.ISmModule;
import com.scene.mesh.foundation.spec.module.extension.ISmExtensionPlugin;

/**
 * 模块包装器，用于包装实现了 ISmModule 接口的模块实例
 * 主要用于添加 JAR 相关的元数据信息
 */
public class SmModuleWrapper implements ISmModule {
    
    private final ISmModule delegate;
    private final String path;
    private final Map<String, Object> metas;
    
    public SmModuleWrapper(ISmModule delegate, String path, Map<String, Object> metas) {
        this.delegate = delegate;
        this.path = path;
        this.metas = metas;
    }
    
    @Override
    public String getId() {
        return delegate.getId();
    }
    
    @Override
    public String getName() {
        return delegate.getName();
    }
    
    @Override
    public String getVersion() {
        return delegate.getVersion();
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    @Override
    public List<ISmExtensionPlugin> getExtensionCapacities() {
        return delegate.getExtensionCapacities();
    }
    
    @Override
    public Map<String, Object> getMetas() {
        return metas;
    }
    
    /**
     * 获取被包装的原始模块实例
     */
    public ISmModule getDelegate() {
        return delegate;
    }
}
