package com.armandow.rest.api;

import com.armandow.rest.annotatios.*;
import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentParamType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import org.json.JSONObject;

import java.io.IOException;

@SuppressWarnings({"java:S2187"})
@RESTController("/params")
public class ParamsControllerTest {
    @RESTMethod(
            path = "/txt/{filename}",
            method = MethodType.GET,
            contentType = ContentType.TEXT_PLAIN,
            produces = ContentType.APPLICATION_OCTET_STREAM,
            producesCharset = CharsetType.UTF_8)
    public byte[] getTxt(@RESTPathParam("filename") String fileName) throws IOException {
        var file = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if ( file != null ) {
            return file.readAllBytes();
        }

        return "".getBytes();
    }

    @RESTMethod(
            path = "/contentParam",
            method = MethodType.POST,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_JSON,
            producesCharset = CharsetType.UTF_8)
    public JSONObject contentParam(@RESTContentParam(ContentParamType.JSON) JSONObject resquest) {
        return resquest;
    }

    @RESTMethod(
            path = "/contentParamText",
            method = MethodType.POST,
            contentType = ContentType.TEXT_PLAIN,
            produces = ContentType.TEXT_PLAIN,
            producesCharset = CharsetType.UTF_8)
    public String contentParamText(@RESTContentParam(ContentParamType.TEXT) String resquest) {
        return resquest;
    }

    @RESTMethod(
            path = "/server",
            method = MethodType.POST,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_JSON,
            producesCharset = CharsetType.UTF_8)
    public JSONObject build(@RESTServerName String serverName,
                            @RESTServerPort String serverPort,
                            @RESTContext String context) throws Exception {
        return new JSONObject()
                .put("serverName", serverName)
                .put("serverPort", serverPort)
                .put("context", context);
    }
}
