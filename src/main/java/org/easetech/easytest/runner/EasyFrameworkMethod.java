package org.easetech.easytest.runner;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;

public class EasyFrameworkMethod extends FrameworkMethod {

    private String methodName = "";
    
    public EasyFrameworkMethod(Method method) {
        super(method);
        methodName = method.getName();
        
    }
    
    /**
     * Returns the method's name
     */
    @Override
    public String getName() {
        return this.methodName;
    }
    
    public void setName(String name){
        this.methodName = name;
    }

}
