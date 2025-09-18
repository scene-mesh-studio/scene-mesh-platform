package com.scene.mesh.module.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扩展插件注解
 * 用于标记扩展实现类，平台加载 module 时会自动扫描并创建ISmExtensionPlugin包装器
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionPlugin {
    /**
     * 扩展槽ID
     * 指定该插件属于哪个扩展槽
     */
    String slotId();

    /**
     * 插件ID
     * 在扩展槽内的唯一标识
     */
    String pluginId();

    /**
     * 插件名称
     * 用于显示和调试
     */
    String name() default "";

    /**
     * 插件描述
     * 用于文档和调试
     */
    String description() default "";

    /**
     * 插件版本
     * 用于版本管理
     */
    String version() default "";

    /**
     * 插件优先级
     * 数值越小优先级越高，默认为100
     */
    int priority() default 100;
}
