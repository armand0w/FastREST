package com.armandow.rest.exceptions;

import com.armandow.rest.interfaces.IHTTPStatusNotifiable;
import lombok.Getter;

import java.io.Serial;

@Getter
public class CustomException extends Exception implements IHTTPStatusNotifiable {

    @Serial
    private static final long serialVersionUID = 4616551084422502801L;
    private final transient Object data;
    private final transient int status;

    public CustomException(String message, Object data, int status) {
        super(message);
        this.data = data;
        this.status = status;
    }

    public CustomException(String message) {
        super(message);
        this.status = 409;
        this.data = null;
    }

    public Integer getHTTPStatus() {
        return status;
    }

}
