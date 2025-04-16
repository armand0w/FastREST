package com.armandow.rest.model;

import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;

import java.lang.reflect.Method;

public class Execution {
    private Method executionMethod;
    private Object executionObject;
    private ContentType executionProduces;
    private CharsetType executionProducesCharset;

    public Method getExecutionMethod() {
        return executionMethod;
    }

    public void setExecutionMethod(Method executionMethod) {
        this.executionMethod = executionMethod;
    }

    public Object getExecutionObject() {
        return executionObject;
    }

    public void setExecutionObject(Object executionObject) {
        this.executionObject = executionObject;
    }

    public ContentType getExecutionProduces() {
        return executionProduces;
    }

    public void setExecutionProduces(ContentType executionProduces) {
        this.executionProduces = executionProduces;
    }

    public CharsetType getExecutionProducesCharset() {
        return executionProducesCharset;
    }

    public void setExecutionProducesCharset(CharsetType executionProducesCharset) {
        this.executionProducesCharset = executionProducesCharset;
    }
}
