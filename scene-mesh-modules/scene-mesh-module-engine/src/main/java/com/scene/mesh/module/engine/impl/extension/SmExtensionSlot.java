package com.scene.mesh.module.engine.impl.extension;

import com.scene.mesh.module.engine.spec.extension.ISmExtensionSlot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 扩展槽实现
 */
public class SmExtensionSlot implements ISmExtensionSlot {
    
    private final String id;
    private final Class<?> slotInterfaceClass;
    private final String slotInterfaceDefinition;
    private final String[] slotInterfaceMethodSignatures;
    
    public SmExtensionSlot(String id, Class<?> slotInterfaceClass) {
        this.id = id;
        this.slotInterfaceClass = slotInterfaceClass;
        this.slotInterfaceDefinition = generateInterfaceDefinition(slotInterfaceClass);
        this.slotInterfaceMethodSignatures = generateMethodSignatures(slotInterfaceClass);
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Class<?> getSlotInterfaceClass() {
        return slotInterfaceClass;
    }
    
    @Override
    public String getSlotInterfaceDefinition() {
        return slotInterfaceDefinition;
    }
    
    @Override
    public String[] getSlotInterfaceMethodSignatures() {
        return slotInterfaceMethodSignatures;
    }
    
    /**
     * 生成接口定义字符串
     */
    private String generateInterfaceDefinition(Class<?> interfaceClass) {
        if (interfaceClass == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("public interface ").append(interfaceClass.getSimpleName());
        
        // 添加泛型信息
        if (interfaceClass.getTypeParameters().length > 0) {
            sb.append("<");
            for (int i = 0; i < interfaceClass.getTypeParameters().length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(interfaceClass.getTypeParameters()[i].getName());
            }
            sb.append(">");
        }
        
        // 添加继承的接口
        Class<?>[] interfaces = interfaceClass.getInterfaces();
        if (interfaces.length > 0) {
            sb.append(" extends ");
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(interfaces[i].getSimpleName());
            }
        }
        
        sb.append(" {");
        
        // 添加方法定义
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() != Object.class) {
                sb.append("\n    ");
                sb.append(generateMethodSignature(method));
                sb.append(";");
            }
        }
        
        sb.append("\n}");
        return sb.toString();
    }
    
    /**
     * 生成方法签名数组
     */
    private String[] generateMethodSignatures(Class<?> interfaceClass) {
        if (interfaceClass == null) {
            return new String[0];
        }
        
        Method[] methods = interfaceClass.getMethods();
        List<String> signatures = new ArrayList<>();
        
        for (Method method : methods) {
            if (method.getDeclaringClass() != Object.class) {
                signatures.add(generateMethodSignature(method));
            }
        }
        
        return signatures.toArray(new String[0]);
    }
    
    /**
     * 生成单个方法的签名
     */
    private String generateMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        
        // 返回类型
        sb.append(method.getReturnType().getSimpleName()).append(" ");
        
        // 方法名
        sb.append(method.getName());
        
        // 参数列表
        sb.append("(");
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramTypes[i].getSimpleName()).append(" arg").append(i);
        }
        sb.append(")");
        
        // 异常声明
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            sb.append(" throws ");
            for (int i = 0; i < exceptionTypes.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(exceptionTypes[i].getSimpleName());
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "SmExtensionSlot{" +
                "id='" + id + '\'' +
                ", slotInterfaceClass=" + slotInterfaceClass +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SmExtensionSlot that = (SmExtensionSlot) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
