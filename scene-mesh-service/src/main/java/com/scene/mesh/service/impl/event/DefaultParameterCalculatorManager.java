//package com.scene.mesh.service.impl.event;
//
//import com.scene.mesh.foundation.spec.parameter.data.calculate.IParameterCalculator;
//import com.scene.mesh.foundation.spec.parameter.data.calculate.IParameterCalculatorManager;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class DefaultParameterCalculatorManager implements IParameterCalculatorManager {
//
//    private final Map<IParameterCalculator.CalculateType,IParameterCalculator> parameterCalculatorMap;
//
//    public DefaultParameterCalculatorManager(List<IParameterCalculator> parameterCalculatorList) {
//        this.parameterCalculatorMap = new ConcurrentHashMap<>();
//        for (IParameterCalculator parameterCalculator : parameterCalculatorList) {
//            this.registerParameterCalculator(parameterCalculator);
//        }
//    }
//
//    @Override
//    public void registerParameterCalculator(IParameterCalculator parameterCalculator) {
//        parameterCalculatorMap.put(parameterCalculator.getCalculateType(),parameterCalculator);
//    }
//
//    @Override
//    public void unregisterParameterCalculator(IParameterCalculator parameterCalculator) {
//        parameterCalculatorMap.remove(parameterCalculator.getCalculateType());
//    }
//
//    @Override
//    public IParameterCalculator getParameterCalculator(IParameterCalculator.CalculateType calculateType) {
//        return this.parameterCalculatorMap.get(calculateType);
//    }
//}
