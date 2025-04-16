package com.armandow.rest.api;

import com.armandow.rest.annotatios.RESTController;
import com.armandow.rest.annotatios.RESTMethod;
import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import org.json.JSONObject;

@SuppressWarnings({"java:S2187"})
@RESTController(value = "/methods")
public class MethodsControllerTest {

    @RESTMethod(
            path = "/post",
            method = MethodType.POST,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_JSON,
            producesCharset = CharsetType.UTF_8)
    public JSONObject testPost() {
        return new JSONObject();
    }

    @RESTMethod(
            path = "/put",
            method = MethodType.PUT,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_JSON,
            producesCharset = CharsetType.UTF_8)
    public JSONObject testPut() {
        return new JSONObject();
    }

    @RESTMethod(
            path = "/get",
            method = MethodType.GET,
            contentType = ContentType.TEXT_PLAIN,
            produces = ContentType.TEXT_PLAIN,
            producesCharset = CharsetType.UTF_8)
    public String testGet() {
        return "ola ke ase!";
    }

}
