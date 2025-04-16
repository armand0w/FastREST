package com.armandow.rest.api;

import com.armandow.rest.annotatios.RESTController;
import com.armandow.rest.annotatios.RESTMethod;
import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import com.armandow.rest.exceptions.AuthorizationException;
import com.armandow.rest.exceptions.ContentException;
import com.armandow.rest.exceptions.CustomException;
import com.armandow.rest.exceptions.OperationExecutionException;
import org.json.JSONObject;

@SuppressWarnings({"java:S2187"})
@RESTController("/exceptions")
public class ExceptionControllerTest {

    @RESTMethod(
            path = "/auth",
            method = MethodType.POST,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_XML,
            producesCharset = CharsetType.UTF_8
    )
    public void getAuthorizationException() throws AuthorizationException {
        try {
            throw new AuthorizationException("Test AuthorizationException");
        } catch (AuthorizationException ae) {
            throw new AuthorizationException(ae.getMessage(), new JSONObject());
        }
    }

    @RESTMethod(
            path = "/content",
            method = MethodType.POST,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_JSON,
            producesCharset = CharsetType.UTF_8
    )
    public void getContentException() throws ContentException {
        try {
            throw new ContentException("Test ContentException");
        } catch (ContentException ae) {
            throw new ContentException(ae.getMessage(), new JSONObject());
        }
    }

    @RESTMethod(
            path = "/custom",
            method = MethodType.POST,
            contentType = ContentType.APPLICATION_JSON,
            produces = ContentType.APPLICATION_JSON,
            producesCharset = CharsetType.UTF_8
    )
    public void getCustomException() throws CustomException {
        try {
            throw new OperationExecutionException("Test CustomException");
        } catch (OperationExecutionException ope) {
            try {
                throw new CustomException(ope.getMessage());
            } catch (CustomException ae) {
                throw new CustomException(ae.getMessage(), new JSONObject(), 418);
            }
        }

    }

}
