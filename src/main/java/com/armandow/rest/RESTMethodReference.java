package com.armandow.rest;

import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class RESTMethodReference {
    private String path;
    private String className;
    private String method;
    private ContentType contentType;
    private ContentType produces;
    private CharsetType producesCharset;
    private MethodType methodType;
}
