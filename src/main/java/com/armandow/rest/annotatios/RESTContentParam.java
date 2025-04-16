package com.armandow.rest.annotatios;

import com.armandow.rest.enums.ContentParamType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RESTContentParam {
    ContentParamType value() default ContentParamType.TEXT;
}
