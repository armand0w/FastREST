package com.armandow.rest.exceptions;

import com.armandow.rest.interfaces.IHTTPStatusNotifiable;
import lombok.Getter;

import java.io.Serial;

@Getter
public class AuthorizationException extends Exception implements IHTTPStatusNotifiable {
    @Serial
    private static final long serialVersionUID = -3561417345404124452L;
    private final transient Object data;

    public AuthorizationException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public AuthorizationException(String message) {
        super(message);
        this.data = null;
    }

    public Integer getHTTPStatus() {
        return 401;
    }

}
