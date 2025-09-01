package com.scene.mesh.foundation.impl.component;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Spring XML AppContext加载工具
 */
public class SpringApplicationContextUtils {

    @lombok.Setter
    private static ApplicationContext applicationContext;
    @lombok.Setter
    @lombok.Getter
    private static String contextId;
    @lombok.Setter
    private static Class<?>[] contextClass;

    public static synchronized ApplicationContext getApplicationContext() {
        if (applicationContext != null) {
            return applicationContext;
        } else {
            //synchronized (lockObj) {
            applicationContext = new ClassPathXmlApplicationContext(contextId == null ? "work.xml" : contextId);
            return applicationContext;
            //}
        }
    }

    public static synchronized ApplicationContext getApplicationContextByAnnotation() {
        if (applicationContext != null) {
            return applicationContext;
        } else {
            //synchronized (lockObj) {
            applicationContext = new AnnotationConfigApplicationContext(contextClass);
            return applicationContext;
            //}
        }
    }

}
