package com.armandow.rest.exceptions;

import com.armandow.rest.interfaces.IHTTPStatusNotifiable;
import lombok.Getter;

import java.io.Serial;

@Getter
public class OperationExecutionException extends Exception implements IHTTPStatusNotifiable {
    @Serial
    private static final long serialVersionUID = -6943756540282297834L;
    private final transient Object data;

    public OperationExecutionException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public OperationExecutionException(String message) {
        super(message);
        this.data = null;
    }
    
    public Integer getHTTPStatus() {
        return 409;
    }
}
