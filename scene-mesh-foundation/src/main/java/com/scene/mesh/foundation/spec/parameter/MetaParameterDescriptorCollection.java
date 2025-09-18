
package com.scene.mesh.foundation.spec.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 参数描述集合
 */
public class MetaParameterDescriptorCollection implements Serializable {

    private final List<MetaParameterDescriptor> parameterDescriptors;

    public MetaParameterDescriptorCollection() {
        this.parameterDescriptors = new ArrayList<>();
    }

    public MetaParameterDescriptorCollection(List<MetaParameterDescriptor> ls) {
        this();
        if (ls != null) {
            this.parameterDescriptors.addAll(ls);
        }
    }

    public MetaParameterDescriptorCollection(MetaParameterDescriptor... descriptors) {
        this();
        if (descriptors != null) this.parameterDescriptors.addAll(Arrays.asList(descriptors));
    }

    public MetaParameterDescriptor findParameterDescriptorByName(String name) {
        for (MetaParameterDescriptor dr : this.parameterDescriptors) {
            if (dr.getName().equals(name)) {
                return dr;
            }
        }
        return null;
    }

    public void addParameterDescriptor(MetaParameterDescriptor dr) {
        this.parameterDescriptors.add(dr);
    }

    public Collection<MetaParameterDescriptor> getParameterDescriptors() {
        return this.parameterDescriptors;
    }

}
