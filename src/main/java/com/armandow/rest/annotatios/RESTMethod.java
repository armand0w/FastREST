package com.armandow.rest.annotatios;

import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RESTMethods.class)
public @interface RESTMethod {
    ContentType contentType() default ContentType.TEXT_PLAIN;

    ContentType produces() default ContentType.TEXT_PLAIN;

    MethodType method() default MethodType.GET;

    CharsetType producesCharset() default CharsetType.NONE;

    String path() default "";
}
