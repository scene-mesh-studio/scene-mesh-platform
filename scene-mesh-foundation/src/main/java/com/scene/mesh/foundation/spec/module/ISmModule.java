package com.scene.mesh.foundation.spec.module;

import java.util.List;
import java.util.Map;

/**
 * Module 定义
 */
public interface ISmModule {

    /**
     * ID
     */
    String getId();

    /**
     * 名称
     */
    String getName();

    /**
     * 版本
     */
    String getVersion();

    /**
     * 模块路径，用于模块加载
     * @return 可能是文件路径和远端 URL路径
     */
    String getPath();

    /**
     * 模块中的扩展能力列表
     * @return 扩展插件 ID 列表
     */
    List<String> getExtensionCapacities();

    /**
     * 元数据
     */
    Map<String,Object> getMetas();
}
