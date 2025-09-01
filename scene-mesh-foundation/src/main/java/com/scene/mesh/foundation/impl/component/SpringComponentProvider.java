package com.scene.mesh.foundation.impl.component;

import com.scene.mesh.foundation.spec.component.IComponentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基于 spring bean 的组件提供者
 */
@Slf4j
public class SpringComponentProvider implements IComponentProvider, Serializable{

    @Serial
    private static final long serialVersionUID = 1684227928905153462L;

    @Override
    public Object getComponent(String componentId) {
        ApplicationContext applicationContext = SpringApplicationContextUtils.getApplicationContext();
        log.info("BeanFactory get {}",componentId);
        return applicationContext.getBean(componentId);
    }
}
